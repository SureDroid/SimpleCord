package com.suredroid.discord;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmbedMessage {
    private String title, message;
    public boolean hasBothValues(){
        return (title != null && message != null);
    }
}
