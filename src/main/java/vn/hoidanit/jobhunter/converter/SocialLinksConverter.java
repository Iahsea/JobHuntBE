package vn.hoidanit.jobhunter.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.hoidanit.jobhunter.domain.SocialLinks;

@Converter(autoApply = true)
public class SocialLinksConverter implements AttributeConverter<SocialLinks, String> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(SocialLinks socialLinks) {
        if (socialLinks == null) return null;
        try {
            return objectMapper.writeValueAsString(socialLinks);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot convert SocialLinks to JSON", e);
        }
    }

    @Override
    public SocialLinks convertToEntityAttribute(String json) {
        if (json == null) return null;
        try {
            return objectMapper.readValue(json, SocialLinks.class);
        } catch (Exception e) {
            throw new RuntimeException("Cannot convert JSON to SocialLinks", e);
        }
    }
}
