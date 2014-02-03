package at.modalog.cordova.plugin.html2pdf;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;

import android.util.Log;

import java.io.File;

public class Html2pdf extends CordovaPlugin
{
	private static final String LOG_TAG = "Html2Pdf";

	/**
	 * Constructor.
	 */
	public Html2pdf() {

	}

    /**
     * Executes the request.
     *
     * This method is called from the WebView thread. To do a non-trivial amount of work, use:
     *     cordova.getThreadPool().execute(runnable);
     *
     * To run on the UI thread, use:
     *     cordova.getActivity().runOnUiThread(runnable);
     *
     * @param action          The action to execute.
     * @param rawArgs         The exec() arguments in JSON form.
     * @param callbackContext The callback context used when calling back into JavaScript.
     * @return                Whether the action was valid.
     */
	@Override
    public boolean execute(String action, String rawArgs, CallbackContext callbackContext) throws JSONException
    {
		JSONArray args = parseArgsWithBigData(rawArgs);

		try
		{
			if( action.equals("create") )
			{
				Log.v(LOG_TAG,"java create html from pdf called");
				Log.v(LOG_TAG, "File: " + args.getString(1));
				Log.v(LOG_TAG, "Html: " + args.getString(0));
				Log.v(LOG_TAG, "" + args.getString(0).substring(0, 30));
				Log.v(LOG_TAG, "" + args.getString(0).substring(args.getString(1).length() - 30));

				// TODO: write html as pdf into a file

				//callbackContext.success("Jippie!");
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
		// ToDo: close all files.
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------


}
