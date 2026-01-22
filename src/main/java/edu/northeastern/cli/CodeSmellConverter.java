package edu.northeastern.cli;

import picocli.CommandLine.TypeConversionException;
import picocli.CommandLine.ITypeConverter;

public class CodeSmellConverter implements ITypeConverter<CodeSmell> {
    @Override
    public CodeSmell convert(String value) throws Exception {
        CodeSmell smell = CodeSmell.fromLabel(value);

        if(smell == null) {
            throw new TypeConversionException(
                    "Unknown code smell: " + value
            );
        }

        return smell;
    }
}
