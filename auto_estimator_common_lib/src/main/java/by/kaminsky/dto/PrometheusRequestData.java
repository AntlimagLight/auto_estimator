package by.kaminsky.dto;

import lombok.*;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrometheusRequestData extends RequestData implements Serializable {

    private int diameter;
    private int height;
    private boolean coverPlate;
    private boolean umbrella;
    private boolean reinforcement;
    private boolean ventilationDuct;

    public PrometheusRequestData(Update update) {
        super(update);
    }

    @Override
    public String toString() {
        return "PrometheusCalculateRequest{" +
                "diameter=" + diameter +
                ", height=" + height +
                ", coverPlate=" + coverPlate +
                ", umbrella=" + umbrella +
                ", reinforcement=" + reinforcement +
                ", ventilationDuct=" + ventilationDuct +
                '}';
    }
}
