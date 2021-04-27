package com.github.kokorin.jaffree.ffprobe;

import com.github.kokorin.jaffree.JaffreeException;
import com.github.kokorin.jaffree.LogLevel;
import com.github.kokorin.jaffree.process.StdReader;
import com.github.kokorin.jaffree.util.LineIterator;
import com.github.kokorin.jaffree.util.LogMessage;
import com.github.kokorin.jaffree.util.LogMessageIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FFprobeLogReader implements StdReader<FFprobeResult> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FFprobeLogReader.class);

    @Override
    public FFprobeResult read(InputStream stdOut) {
        LogMessageIterator logMessageIterator = new LogMessageIterator(
                new LineIterator(
                        new BufferedReader(new InputStreamReader(stdOut))
                )
        );

        String errorMessage = null;
        while (logMessageIterator.hasNext()) {
            LogMessage logMessage = logMessageIterator.next();

            if (logMessage.logLevel != null) {
                switch (logMessage.logLevel) {
                    case TRACE:
                        LOGGER.trace(logMessage.message);
                        break;
                    case VERBOSE:
                    case DEBUG:
                        LOGGER.debug(logMessage.message);
                        break;
                    case INFO:
                        LOGGER.info(logMessage.message);
                        break;
                    case WARNING:
                        LOGGER.warn(logMessage.message);
                        break;
                    case ERROR:
                    case FATAL:
                    case PANIC:
                    case QUIET:
                        LOGGER.error(logMessage.message);
                        break;
                }
            } else {
                LOGGER.info(logMessage.message);
            }

            if (logMessage.logLevel != null && logMessage.logLevel.code() <= LogLevel.ERROR.code()) {
                if (errorMessage == null) {
                    errorMessage = logMessage.message;
                } else {
                    errorMessage += "\n" + logMessage.message;
                }
            }
        }

        if (errorMessage != null) {
            throw new JaffreeException("Finished with error message: " + errorMessage);
        }

        return null;
    }
}