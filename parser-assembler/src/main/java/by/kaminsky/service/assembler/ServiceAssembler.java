package by.kaminsky.service.assembler;

import by.kaminsky.service.parse.ParseService;

import java.util.List;

public interface ServiceAssembler {

    List<ParseService> getEnabledParsingServices();

    void disableParsingService(String string);

    void enableParsingService(String string);
}
