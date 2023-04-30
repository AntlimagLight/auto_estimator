package by.kaminsky.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RequestData {

    protected Update update;

}
