package net.osaris.exterminate.tools;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.zip.CRC32;
import java.util.zip.CheckedOutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.utils.ByteArray;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.StreamUtils;

public class ScreenshotFactory {
	private static PNG writer = new PNG((int)(1920*1080 * 1.5f)); // Guess at deflated size.

    private static int counter = 1;
    public static void saveScreenshot(){
        try{
            FileHandle fh;
            do{
                fh = new FileHandle("screenshot" + counter++ + ".png");
            }while (fh.exists());
            Pixmap pixmap = getScreenshot(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), false);
 			try {
				writer.setFlipY(true);
				writer.write(fh, pixmap);
			} finally {
	//			writer.dispose();
			}
            pixmap.dispose();
            
        }catch (Exception e){           
        }
    }

    private static Pixmap getScreenshot(int x, int y, int w, int h, boolean yDown){
    	   	
        Gdx.gl.glPixelStorei(GL20.GL_PACK_ALIGNMENT, 1);

		final Pixmap pixmap = new Pixmap(w, h, Format.RGB888);
		ByteBuffer pixels = pixmap.getPixels();
		Gdx.gl.glReadPixels(x, y, w, h, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, pixels);

        return pixmap;
    }
    
    
	static public class PNG implements Disposable {
		static private final byte[] SIGNATURE = {(byte)137, 80, 78, 71, 13, 10, 26, 10};
		static private final int IHDR = 0x49484452, IDAT = 0x49444154, IEND = 0x49454E44;
		static private final byte COLOR_ARGB = 6;
		static private final byte COMPRESSION_DEFLATE = 0;
		static private final byte FILTER_NONE = 0;
		static private final byte INTERLACE_NONE = 0;
		static private final byte PAETH = 4;

		private final ChunkBuffer buffer;
		private final DeflaterOutputStream deflaterOutput;
		private final Deflater deflater;
		private ByteArray lineOutBytes, curLineBytes, prevLineBytes;
		private boolean flipY = true;
		private int lastLineLen;

		public PNG () {
			this(128 * 128);
		}

		public PNG (int initialBufferSize) {
			buffer = new ChunkBuffer(initialBufferSize);
			deflater = new Deflater();
			deflaterOutput = new DeflaterOutputStream(buffer, deflater);
		}

		/** If true, the resulting PNG is flipped vertically. Default is true. */
		public void setFlipY (boolean flipY) {
			this.flipY = flipY;
		}

		/** Sets the deflate compression level. Default is {@link Deflater#DEFAULT_COMPRESSION}. */
		public void setCompression (int level) {
			deflater.setLevel(level);
		}

		public void write (FileHandle file, Pixmap pixmap) throws IOException {
			OutputStream output = file.write(false);
			try {
				write(output, pixmap);
			} finally {
				StreamUtils.closeQuietly(output);
			}
		}

		/** Writes the pixmap to the stream without closing the stream. */
		public void write (OutputStream output, Pixmap pixmap) throws IOException {
			DataOutputStream dataOutput = new DataOutputStream(output);
			dataOutput.write(SIGNATURE);

			buffer.writeInt(IHDR);
			buffer.writeInt(pixmap.getWidth());
			buffer.writeInt(pixmap.getHeight());
			buffer.writeByte(8); // 8 bits per component.
			buffer.writeByte(COLOR_ARGB);
			buffer.writeByte(COMPRESSION_DEFLATE);
			buffer.writeByte(FILTER_NONE);
			buffer.writeByte(INTERLACE_NONE);
			buffer.endChunk(dataOutput);

			buffer.writeInt(IDAT);
			deflater.reset();

			int lineLen = pixmap.getWidth() * 4;
			byte[] lineOut, curLine, prevLine;
			if (lineOutBytes == null) {
				lineOut = (lineOutBytes = new ByteArray(lineLen)).items;
				curLine = (curLineBytes = new ByteArray(lineLen)).items;
				prevLine = (prevLineBytes = new ByteArray(lineLen)).items;
			} else {
				lineOut = lineOutBytes.ensureCapacity(lineLen);
				curLine = curLineBytes.ensureCapacity(lineLen);
				prevLine = prevLineBytes.ensureCapacity(lineLen);
				for (int i = 0, n = lastLineLen; i < n; i++)
					prevLine[i] = 0;
			}
			lastLineLen = lineLen;

			ByteBuffer pixels = pixmap.getPixels();
			int oldPosition = pixels.position();
			boolean rgba8888 = pixmap.getFormat() == Format.RGBA8888;
			for (int y = 0, h = pixmap.getHeight(); y < h; y++) {
				int py = flipY ? (h - y - 1) : y;
				if (rgba8888) {
					pixels.position(py * lineLen);
					pixels.get(curLine, 0, lineLen);
				} else {
					for (int px = 0, x = 0; px < pixmap.getWidth(); px++) {
						int pixel = pixmap.getPixel(px, py);
						curLine[x++] = (byte)((pixel >> 24) & 0xff);
						curLine[x++] = (byte)((pixel >> 16) & 0xff);
						curLine[x++] = (byte)((pixel >> 8) & 0xff);
						curLine[x++] = (byte)(0xff);
					}
				}

				lineOut[0] = (byte)(curLine[0] - prevLine[0]);
				lineOut[1] = (byte)(curLine[1] - prevLine[1]);
				lineOut[2] = (byte)(curLine[2] - prevLine[2]);
				lineOut[3] = (byte)(curLine[3] - prevLine[3]);

				for (int x = 4; x < lineLen; x++) {
					int a = curLine[x - 4] & 0xff;
					int b = prevLine[x] & 0xff;
					int c = prevLine[x - 4] & 0xff;
					int p = a + b - c;
					int pa = p - a;
					if (pa < 0) pa = -pa;
					int pb = p - b;
					if (pb < 0) pb = -pb;
					int pc = p - c;
					if (pc < 0) pc = -pc;
					if (pa <= pb && pa <= pc)
						c = a;
					else if (pb <= pc) //
						c = b;
					lineOut[x] = (byte)(curLine[x] - c);
				}

				deflaterOutput.write(PAETH);
				deflaterOutput.write(lineOut, 0, lineLen);

				byte[] temp = curLine;
				curLine = prevLine;
				prevLine = temp;
			}
			pixels.position(oldPosition);
			deflaterOutput.finish();
			buffer.endChunk(dataOutput);

			buffer.writeInt(IEND);
			buffer.endChunk(dataOutput);

			output.flush();
		}

		/** Disposal will happen automatically in {@link #finalize()} but can be done explicitly if desired. */
		public void dispose () {
			deflater.end();
		}

		static class ChunkBuffer extends DataOutputStream {
			final ByteArrayOutputStream buffer;
			final CRC32 crc;

			ChunkBuffer (int initialSize) {
				this(new ByteArrayOutputStream(initialSize), new CRC32());
			}

			private ChunkBuffer (ByteArrayOutputStream buffer, CRC32 crc) {
				super(new CheckedOutputStream(buffer, crc));
				this.buffer = buffer;
				this.crc = crc;
			}

			public void endChunk (DataOutputStream target) throws IOException {
				flush();
				target.writeInt(buffer.size() - 4);
				buffer.writeTo(target);
				target.writeInt((int)crc.getValue());
				buffer.reset();
				crc.reset();
			}
		}
	}

}