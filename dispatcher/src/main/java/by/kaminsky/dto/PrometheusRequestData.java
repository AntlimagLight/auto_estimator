package by.kaminsky.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PrometheusRequestData {

    public PrometheusRequestData(Update update) {
        this.update = update;
    }

    private Update update;
    @NotNull
    private int diameter;
    @Min(3000)
    @Max(20000)
    @NotNull
    private int height;
    private boolean coverPlate;
    private boolean umbrella;
    private boolean reinforcement;
    private boolean ventilationDuct;

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
