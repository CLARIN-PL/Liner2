package liner2.reader;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import liner2.structure.TokenAttributeIndex;
import liner2.structure.Paragraph;

import liner2.tools.DataFormatException;

import liner2.LinerOptions;

public class PlainTextStreamReader extends StreamReader {
	private static final String CMD_MORPH = "{PATH}maca-analyse -qs morfeusz-nkjp -o ccl";
	private static final String CMD_TAGGER = "{PATH}wmbt/wmbt.py -d {PATH}model_nkjp10 -i ccl -o ccl {PATH}config/nkjp-k11.ini -";

	private InputStream inputStream;
	private StreamReader cclStreamReader;
	private boolean init = false;
	
	public PlainTextStreamReader(InputStream is) {
		this.inputStream = is;
	}

	/**
	 * Send data to tagger, read it back from CCL output
     */
	protected void init() throws DataFormatException {
		if (this.init)
			return;

		String maca_path = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_MACA);
		String wmbt_path = LinerOptions.getGlobal().getOption(LinerOptions.OPTION_WMBT);
		if (maca_path == null)
			throw new DataFormatException("Plain text reader: requires morphological analyzer (set -maca option).");
		if (maca_path.equals("-")) 
			maca_path = "";
		else if (!maca_path.endsWith("/"))
			maca_path += "/";

		// prepare maca command
		String maca_cmd = this.CMD_MORPH;
		maca_cmd = maca_cmd.replaceAll("\\{PATH\\}", maca_path);

		// execute maca
		Process maca_p = null;
		try {
			maca_p = Runtime.getRuntime().exec(maca_cmd);
		} catch (IOException ex) {
			throw new DataFormatException("Failed to run morphological analyzer.\nCommand: " + maca_cmd);
		}

		InputStream maca_in = maca_p.getInputStream();
		OutputStream maca_out = maca_p.getOutputStream();

		// send text to maca
		BufferedReader input_reader = new BufferedReader(new InputStreamReader(this.inputStream));
		BufferedWriter maca_writer = new BufferedWriter(new OutputStreamWriter(maca_out));		
		try {
			String line = null;
			while ((line = input_reader.readLine()) != null)
				maca_writer.write(line, 0, line.length());
			maca_writer.close();
		} catch (IOException ex) {
			throw new DataFormatException("I/O error while tagging text.");
		}

		if (wmbt_path != null) {
			// prepare WMBT command
			if (!wmbt_path.endsWith("/"))
				wmbt_path += "/";
			String wmbt_cmd = this.CMD_TAGGER;
			wmbt_cmd = wmbt_cmd.replaceAll("\\{PATH\\}", wmbt_path);

			// execute WMBT
			Process wmbt_p = null;
			try {
				wmbt_p = Runtime.getRuntime().exec(wmbt_cmd);
			} catch (IOException ex) {
				maca_p.destroy();
				throw new DataFormatException("Failed to run tagger.");
			}
			
			InputStream wmbt_in = wmbt_p.getInputStream();
			OutputStream wmbt_out = wmbt_p.getOutputStream();

			// read text from maca and write to WMBT
			BufferedReader maca_reader = new BufferedReader(new InputStreamReader(maca_in));
			BufferedWriter wmbt_writer = new BufferedWriter(new OutputStreamWriter(wmbt_out));
			try {
				String line = null;
				while ((line = maca_reader.readLine()) != null)
					wmbt_writer.write(line, 0, line.length());
				wmbt_writer.close();
			} catch (IOException ex) {
				throw new DataFormatException("I/O error while tagging text.");
			}

			// read CCL output from wmbt
			try {
				this.cclStreamReader = ReaderFactory.get().getStreamReader(wmbt_in,  "ccl");
			} catch (Exception ex) {
				throw new DataFormatException("Could not read tagger output.");
			}
		}
		else {
			// read CCL output from maca
			try {
				this.cclStreamReader = ReaderFactory.get().getStreamReader(maca_in, "ccl");
			} catch (Exception ex) {
				throw new DataFormatException("Could not read tagger output.");
			}
		}

		this.init = true;
	}

	@Override
	public void close() throws DataFormatException {
		try {
			this.cclStreamReader.close();
			this.inputStream.close();
		} catch (IOException ex) {
			throw new DataFormatException("Failed to close input stream.");
		}
	}

	@Override
	public boolean paragraphReady() throws DataFormatException {
		if (!this.init)
			init();
		return this.cclStreamReader.paragraphReady();
	}

	@Override
	protected TokenAttributeIndex getAttributeIndex() {
		if (!this.init) {
			try {
				init();
			} catch (DataFormatException ex) {
				ex.printStackTrace();
			}
		}
		return this.cclStreamReader.getAttributeIndex();
	}

	@Override
	protected Paragraph readRawParagraph() throws DataFormatException {
		if (!this.init)
			init();
		return this.cclStreamReader.readRawParagraph();
	}	
}
