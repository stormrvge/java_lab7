package io;

import connection.server.Server;

import java.io.*;

/**
 *     This class handles file I/O:
 *         read with BufferedReader,
 *         write with FileWriter.
 */

public class FileHandler {
    public static final byte READ = 0, WRITE = 1, READ_WRITE = 2;

    private final byte mod;
    private BufferedReader file_reader;
    private FileWriter file_writer;

    /**
     * Create file handler.
     * @param file_name Path to file to work with
     * @throws IOException If I/O errors occurs.
     */
    public FileHandler(String file_name, byte mod) throws IOException {
        this.mod = mod;
        try {
            this.file_reader = new BufferedReader(new FileReader(file_name));
        } catch (IOException e) {
            if(mod == READ || mod == READ_WRITE) {
                System.err.println("Cannot open file for reading:\n\t" + Server.parseIOException(e));
                throw e;
            }
        }

        try {
            this.file_writer = new FileWriter(file_name, true);
        } catch (IOException e) {
            if(mod == WRITE || mod == READ_WRITE) {
                System.err.println("Cannot open file for writing:\n\t" + Server.parseIOException(e));
                throw e;
            }
        }
    }

    /**
     * Read from file.
     * @return Line in file till '\n' or '\r' or null if end of file
     * @throws IOException If I/O errors occurs.
     */
    public String readline() throws IOException {
        if(mod == READ || mod == READ_WRITE) {
            return file_reader.readLine();
        } else {
            return null;
        }
    }

    /**
     * Close files reader/writer.
     * @throws IOException  If I/O errors occurs.
     */
    public void close() throws IOException {
        if(mod == WRITE || mod == READ_WRITE) {
            file_writer.flush();
            file_writer.close();
        }
        if(mod == READ || mod == READ_WRITE) {
            file_reader.close();
        }
    }
}