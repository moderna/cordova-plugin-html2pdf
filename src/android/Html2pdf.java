package at.modalog.cordova.plugin.html2pdf;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import org.json.JSONArray;
import org.json.JSONException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.pdf.PdfWriter;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.util.Log;

@TargetApi(19)
public class Html2pdf extends CordovaPlugin
{
	private static final String LOG_TAG = "Html2Pdf";
	private CallbackContext callbackContext;
	
	// change your path on the sdcard here
	private String publicTmpDir = "at.modalog.cordova.plugin.html2pdf"; // prepending a dot "." will make it hidden
	private String tmpPdfName = "print.pdf";

	/**
	 * Constructor.
	 */
	public Html2pdf() {

	}

    @Override
    public boolean execute (String action, JSONArray args, CallbackContext callbackContext) throws JSONException
    {
		try
		{
			if( action.equals("create") )
			{
				Log.v(LOG_TAG,"java create html from pdf called");
				Log.v(LOG_TAG, "File: " + args.getString(1));
				Log.v(LOG_TAG, "Html: " + args.getString(0));
				Log.v(LOG_TAG, "Html start:" + args.getString(0).substring(0, 30));
				Log.v(LOG_TAG, "Html end:" + args.getString(0).substring(args.getString(0).length() - 30));
				
		        final Html2pdf self = this;
		        final String content = args.optString(0, "<html></html>");
		        this.callbackContext = callbackContext;

		        cordova.getActivity().runOnUiThread( new Runnable() {
		            public void run()
					{
		                self.loadContentIntoWebView(content);
		            }
		        });
		        
		        // send "no-result" result to delay result handling
		        PluginResult pluginResult = new  PluginResult(PluginResult.Status.NO_RESULT); 
		        pluginResult.setKeepCallback(true); 
		        callbackContext.sendPluginResult(pluginResult);
		        
				return true;
			}
			return false;
		}
		catch (JSONException e)
		{
			// TODO: signal JSON problem to JS
			//callbackContext.error("Problem with JSON");
			return false;
		}
    }


	/**
	 *
	 * Clean up and close all open files.
	 *
	 */
	@Override
	public void onDestroy()
	{
		// ToDo: clean up.
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------
	

    /**
     * Loads the html content into a WebView, saves it as a single multi page pdf file and
     * calls startPdfApp() once it´s done.
     */
    private void loadContentIntoWebView (String content)
    {
              Activity ctx = cordova.getActivity();
        final WebView page = new WebView(ctx);
        final Html2pdf self = this;
        
        page.setVisibility(View.INVISIBLE);
        page.getSettings().setJavaScriptEnabled(false);
        page.setDrawingCacheEnabled(true);
        
        page.setWebViewClient( new WebViewClient() {
            @Override
            public void onPageFinished(final WebView page, String url) {
                new Handler().postDelayed( new Runnable() {
                  @Override
                  public void run()
                  {
                        // slice the web screenshot into pages and save as pdf
                        File tmpFile = self.saveWebViewAsPdf(getWebViewAsBitmap(page));

                        // add pdf as stream to the print intent
                        Intent pdfViewIntent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(tmpFile));
                        pdfViewIntent.setType("application/pdf");

                        // remove the webview
                        ViewGroup vg = (ViewGroup)(page.getParent());
                        vg.removeView(page);
                        
		                // send success result to cordova
		                PluginResult result = new PluginResult(PluginResult.Status.OK);
		                result.setKeepCallback(false); 
	                    self.callbackContext.sendPluginResult(result);
                        
                        // start the pdf viewer app(trigger the pdf view intent)
                        self.cordova.startActivityForResult(self, pdfViewIntent, 0);
                  }
                }, 500);
            }
        });

        // Set base URI to the assets/www folder
        String baseURL = webView.getUrl();
               baseURL = baseURL.substring(0, baseURL.lastIndexOf('/') + 1);

        ctx.addContentView(page, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        page.loadDataWithBaseURL(baseURL, content, "text/html", "utf-8", null);
    }
    
    /**
     * Takes a WebView and returns a Bitmap representation of it (takes a "screenshot").
     * @param WebView
     * @return Bitmap
     */
    Bitmap getWebViewAsBitmap(WebView view)
    {
    	Bitmap b; 
    			
        //Get the dimensions of the view so we can re-layout the view at its current size
        //and create a bitmap of the same size 
        int width = view.getWidth();
        int height = view.getContentHeight();

        if( width == 0 || height == 0 )
        {
            // return error answer to cordova
        	String msg = "Width or height of webview content is 0. Webview to bitmap conversion failed.";
        	Log.e(LOG_TAG, msg );
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, msg);
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
            
            b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        	return b;
        }
        
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = height; // View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        //Cause the view to re-layout
        view.measure(measuredWidth, measuredHeight);
        view.layout(0, 0, width, height);//view.getMeasuredHeight());

        //Create a bitmap backed Canvas to draw the view into
        b = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);

        // draw the view into the canvas
        view.draw(c);
        
        return b;
    }

    /**
     * Slices the screenshot into pages, merges those into a single pdf
     * and saves it in the public accessible /sdcard dir.
     */
    private File saveWebViewAsPdf(Bitmap screenshot) {
        try {
        	
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File (sdCard.getAbsolutePath() + "/" + this.publicTmpDir + "/");
            dir.mkdirs();
            File file;
            FileOutputStream stream;
            
            double pageWidth  = PageSize.A4.getWidth()  * 0.85; // width of the image is 85% of the page
            double pageHeight = PageSize.A4.getHeight() * 0.80; // max height of the image is 80% of the page
            double pageHeightToWithRelation = pageHeight / pageWidth; // e.g.: 1.33 (4/3)
            
            Bitmap currPage;
            int totalSize  = screenshot.getHeight();
            int currPos = 0;
            int currPageCount = 0;
            int sliceWidth = screenshot.getWidth();
            int sliceHeight = (int) Math.round(sliceWidth * pageHeightToWithRelation);
            while( totalSize > currPos && currPageCount < 100  ) // max 100 pages
            {
            	currPageCount++;
            	
            	Log.v(LOG_TAG, "Creating page nr. " + currPageCount );
            	
            	// slice bitmap
            	currPage = Bitmap.createBitmap(screenshot, 0, currPos, sliceWidth, (int) Math.min( sliceHeight, totalSize - currPos ));
            	
            	// save page as png
            	stream = new FileOutputStream( new File(dir, "pdf-page-"+currPageCount+".png") );
            	currPage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                stream.close();
                
                // move current position indicator
            	currPos += sliceHeight;
            }
            
            // create pdf
            Document document = new Document();
            File filePdf = new File(dir, this.tmpPdfName); // change the output name of the pdf here
            PdfWriter.getInstance(document,new FileOutputStream(filePdf));
            document.open();
            for( int i=1; i<=currPageCount; ++i )
            {
            	file = new File(dir, "pdf-page-"+i+".png");
            	Image image = Image.getInstance (file.getAbsolutePath());
                image.scaleToFit( (float)pageWidth, 9999);
            	image.setAlignment(Element.ALIGN_CENTER);
            	document.add(image);
            	document.newPage();
            }
            document.close();
            
            // delete tmp image files
            for( int i=1; i<=currPageCount; ++i )
            {
            	file = new File(dir, "pdf-page-"+i+".png");
            	file.delete();
            }
            
            return filePdf;
            
        } catch (IOException e) {
        	Log.e(LOG_TAG, "ERROR: " + e.getMessage());
            e.printStackTrace();
            // return error answer to cordova
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
        } catch (DocumentException e) {
        	Log.e(LOG_TAG, "ERROR: " + e.getMessage());
			e.printStackTrace();
            // return error answer to cordova
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(false);
            callbackContext.sendPluginResult(result);
		}
        
        Log.v(LOG_TAG, "Uncaught ERROR!");

        return null;
    }


}
