package com.app.cointrack;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles(profiles = {"test"})
@AutoConfigureMockMvc
public abstract class IntegrationTest {
    @Autowired
    protected MockMvc mockMvc;

    protected final static ObjectMapper mapper = new ObjectMapper();

    protected byte[] convertObjectToJsonBytes(Object object)
            throws IOException {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        JavaTimeModule module = new JavaTimeModule();
        mapper.registerModule(module);
        return mapper.writeValueAsBytes(object);
    }
}
