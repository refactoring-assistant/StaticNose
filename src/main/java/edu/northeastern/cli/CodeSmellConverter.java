package edu.northeastern.cli;

import picocli.CommandLine.TypeConversionException;
import picocli.CommandLine.ITypeConverter;

/**
 * Type convertor class for picocli to convert a code smell from a string to its label.
 */
public class CodeSmellConverter implements ITypeConverter<CodeSmell> {
    @Override
    public CodeSmell convert(String value) {
        CodeSmell smell = CodeSmell.fromLabel(value);

        if(smell == null) {
            throw new TypeConversionException(
                    "Unknown code smell: " + value
            );
        }

        return smell;
    }
}
