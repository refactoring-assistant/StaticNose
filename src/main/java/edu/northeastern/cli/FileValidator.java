package edu.northeastern.cli;

import picocli.CommandLine.ITypeConverter;
import picocli.CommandLine.TypeConversionException;

import java.io.File;

public class FileValidator implements ITypeConverter<File> {

    @Override
    public File convert(String value) {
        File file = new File(value);

        if (!file.exists()) {
            throw new TypeConversionException("Error: Path '" + value + "' does not exist.");
        }

        if (!file.isFile()) {
            throw new TypeConversionException("Error: Path '" + value + "' is not a directory. Please provide a folder.");
        }

        if (!file.canRead()) {
            throw new TypeConversionException("Error: Directory '" + value + "' is not readable (permission denied).");
        }

        return file;
    }
}
