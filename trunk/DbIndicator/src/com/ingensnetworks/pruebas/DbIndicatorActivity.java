package com.ingensnetworks.pruebas;

import java.io.IOException;
import java.util.Formatter;

import android.app.Activity;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class DbIndicatorActivity extends Activity implements SeekBar.OnSeekBarChangeListener {
	private MediaRecorder mRecorder = null;
	private boolean listening;
	private TextView decibelsTx;
	private RelativeLayout barDB;
	private TextView decibeliosSeekbar;
	private SeekBar seekbarDB;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        decibelsTx = (TextView) findViewById(R.id.decibels);
        barDB = (RelativeLayout) findViewById(R.id.bardb);
        seekbarDB = (SeekBar) findViewById(R.id.seekbar_db);
        decibeliosSeekbar = (TextView) findViewById(R.id.decibelios_seekbar);
        
        seekbarDB.setOnSeekBarChangeListener(this);
    }
    
    @Override
    protected void onResume() {
    	listening = true;
    	new Ear().execute();
    	super.onResume();
    }
    
    @Override
    protected void onPause() {
    	listening = false;
    	super.onPause();
    }
    
    //Metodo que inicializa la escucha
    public void start() {
		if (mRecorder == null) {
			//Inicializamos los parametros del grabador
			mRecorder = new MediaRecorder();
			mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			
			//No indicamos ningun archivo ya que solo queremos escuchar
			mRecorder.setOutputFile("/dev/null");

			try {
				mRecorder.prepare();
			} catch (IllegalStateException e) {
				Log.e("error", "IllegalStateException");
			} catch (IOException e) {
				Log.e("error", "IOException");
				;
			}

			mRecorder.start();
		}
	}
    
    //Para la escucha
    public void stop() {
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}
	}
    
    //Devuelve la mayor amplitud del sonido captado desde la ultima vez que se llamo al metodo
    public double getAmplitude() {
		if (mRecorder != null)
			return (mRecorder.getMaxAmplitude());
		else
			return 0;
    }
    
    public class Ear extends AsyncTask<Void, Double, Void> {
		
		protected void onPreExecute() {
			start();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			
			while(listening) {
				SystemClock.sleep(200); // Si es menor casi siempre da 0
				Double amplitude = 20 * Math.log10(getAmplitude() / 32768.0);
				publishProgress(amplitude);
			}
		
			return null;
		}
		
		@Override
		protected void onProgressUpdate(Double... values) {
			Double value = values[0];
			
			if (value < -80) {
				value = new Double(-80);
			} else if (value > 0) {
				value = new Double(0);
			}
			
			String db = new Formatter().format("%03.1f",value).toString();
			
			decibelsTx.setText(db + " dB");
			
			updateBar(value);

		}

		@Override
		protected void onPostExecute(Void result) {
			stop();
		}
		
		public void updateBar(Double db) {
			Double width;
			
			// Factor de escala para convertir a Dips
			final float scale = getResources().getDisplayMetrics().density;
						
			width = (db * 250 * scale) / -80; // Anchura en pixeles
			
			
			RelativeLayout.LayoutParams lyParams = new RelativeLayout.LayoutParams(width.intValue(), barDB.getHeight());
			lyParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);			
			barDB.setLayoutParams(lyParams);

		}

	}

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		decibeliosSeekbar.setText((-80 + progress) + " dB");		
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

}