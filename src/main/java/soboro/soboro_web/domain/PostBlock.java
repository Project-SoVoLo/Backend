package soboro.soboro_web.domain;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostBlock {
    private String type;
    private String content;
    private String url;
    private String alt;
}
