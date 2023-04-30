package by.kaminsky.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PrometheusRequestData that = (PrometheusRequestData) o;

        if (diameter != that.diameter) return false;
        if (height != that.height) return false;
        if (coverPlate != that.coverPlate) return false;
        if (umbrella != that.umbrella) return false;
        if (reinforcement != that.reinforcement) return false;
        return ventilationDuct == that.ventilationDuct;
    }

    @Override
    public int hashCode() {
        int result = diameter;
        result = 31 * result + height;
        result = 31 * result + (coverPlate ? 1 : 0);
        result = 31 * result + (umbrella ? 1 : 0);
        result = 31 * result + (reinforcement ? 1 : 0);
        result = 31 * result + (ventilationDuct ? 1 : 0);
        return result;
    }
}
