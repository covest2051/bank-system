package entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Decision {
    private boolean approved;
    private String reason; // null если апруваем

    public static Decision approve() {
        return new Decision(true, null);
    }

    public static Decision reject(String reason) {
        return new Decision(false, reason);
    }
}

