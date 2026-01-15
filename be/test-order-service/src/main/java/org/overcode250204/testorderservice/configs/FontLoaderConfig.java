package org.overcode250204.testorderservice.configs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import jakarta.annotation.PostConstruct;

import java.awt.*;
import java.io.InputStream;

@Configuration
@Slf4j
public class FontLoaderConfig {
    @PostConstruct
    public void registerFonts() {
        try {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();

            try (InputStream normal = getClass().getResourceAsStream("/fonts/calibri.ttf")) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, normal));
            }
            try (InputStream bold = getClass().getResourceAsStream("/fonts/calibrib.ttf")) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, bold));
            }
            try (InputStream italic = getClass().getResourceAsStream("/fonts/calibrii.ttf")) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, italic));
            }
            try (InputStream boldItalic = getClass().getResourceAsStream("/fonts/calibriz.ttf")) {
                ge.registerFont(Font.createFont(Font.TRUETYPE_FONT, boldItalic));
            }

            log.info("Calibri fonts registered!");
        } catch (Exception e) {
            log.error("Fail to Calibri fonts: {}", e.getMessage());
        }
    }
}
