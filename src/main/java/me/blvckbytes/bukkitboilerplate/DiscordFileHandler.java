/*
 * MIT License
 *
 * Copyright (c) 2023 BlvckBytes
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package me.blvckbytes.bukkitboilerplate;

import me.blvckbytes.utilitytypes.ETriResult;
import org.bukkit.plugin.java.JavaPlugin;
import org.codehaus.plexus.util.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystems;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DiscordFileHandler implements IFileHandler {

  private final ClassLoader classLoader;
	private final File currentDirectory;

  public DiscordFileHandler(
		final @NotNull ClassLoader classLoader
	) {
    this.classLoader = classLoader;
		this.currentDirectory = new File(FileSystems.getDefault().getPath(".").toUri());
	}

  @Override
  public @Nullable FileInputStream openForReading(String path) throws IOException {
    File file = new File(this.currentDirectory, path);

    if (!(file.exists() && file.isFile()))
      return null;

    return new FileInputStream(file);
  }

  @Override
  public @Nullable FileOutputStream openForWriting(String path) throws IOException {
    File file = new File(this.currentDirectory, path);

    // Path is a directory, don't overwrite
    if (file.exists() && !file.isFile())
      return null;

    // Ensure parent folder existence
    if (!file.getParentFile().exists()) {
      if (!file.getParentFile().mkdirs())
        return null;
    }

    return new FileOutputStream(file);
  }

  @Override
  public String getAbsolutePath(String path) {
    return new File(this.currentDirectory, path).getAbsolutePath();
  }

  @Override
  public boolean doesFileExist(String path) {
    File file = new File(this.currentDirectory, path);
    return file.exists() && file.isFile();
  }

  @Override
	public void saveResource(String path) {
		if (path != null && ! path.isEmpty()) {
			path = path.replace('\\', '/');
			InputStream in = this.getResource(path);
			if (in == null) {
				throw new IllegalArgumentException("The embedded resource '" + path + "' cannot be found in " + this.currentDirectory);
			} else {
				File outFile = new File(this.currentDirectory, path);
				int lastIndex = path.lastIndexOf(47);
				File outDir = new File(this.currentDirectory, path.substring(0,
																																		 Math.max(
																																			 lastIndex,
																																			 0
																																		 )
				));
				if (!outDir.exists())
					outDir.mkdirs();

				try {
					if (outFile.exists()) {
						Logger.getAnonymousLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile + " because it's already existing.");
					} else {
						OutputStream out = new FileOutputStream(outFile);
						byte[] buf = new byte[1024];

						int len;
						while((len = in.read(buf)) > 0) {
							out.write(buf, 0, len);
						}

						out.close();
						in.close();
					}
				} catch (IOException var10) {
					Logger.getAnonymousLogger().log(Level.SEVERE, "Could not save " + outFile.getName() + " to " + outFile, var10);
				}

			}
		} else {
			throw new IllegalArgumentException("ResourcePath cannot be null or empty");
		}
	}

	@Override
	public void saveResource(
		final String path,
		final File fileToSave
	) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	@Override
  public @Nullable InputStream getResource(
		final @NotNull String path
	) {
		final URL url = this.classLoader.getResource(path);
		try {
			return url == null ? null : url.openStream();
		} catch (
			final IOException exception
		) {
			throw new RuntimeException(exception);
		}
	}

  @Override
  public ETriResult makeDirectories(String path) {
    File file = new File(this.currentDirectory, path);

    if (file.isDirectory())
      return ETriResult.EMPTY;

    if (file.mkdirs())
      return ETriResult.SUCCESS;

    return ETriResult.ERROR;
  }
}