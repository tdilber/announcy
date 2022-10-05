package com.beyt.anouncy.user.controller;

import com.beyt.anouncy.user.TestUtil;
import com.beyt.anouncy.user.UserApplication;
import com.beyt.anouncy.user.dto.UserResolveResultDTO;
import com.beyt.anouncy.user.dto.UserSignInDTO;
import com.beyt.anouncy.user.dto.UserSignUpDTO;
import com.beyt.anouncy.user.entity.Configuration;
import com.beyt.anouncy.user.entity.User;
import com.beyt.anouncy.user.repository.ConfigurationRepository;
import com.beyt.anouncy.user.repository.UserRepository;
import com.beyt.anouncy.user.service.ConfigurationService;
import com.beyt.anouncy.user.service.JwtTokenProvider;
import com.beyt.anouncy.user.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = UserApplication.class)
@AutoConfigureMockMvc
class UserControllerTestIT {

    public static final String TEST_USER = "test-user";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ConfigurationRepository configurationRepository;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @BeforeAll
    void beforeAll() {
        configurationRepository.save(new Configuration("anouncy.jwtToken.secret", "asdasdasdasdasdasdASDASdjshdkjahsdk12312312ASDasd123"));
        configurationRepository.save(new Configuration("anouncy.password.salt.user", "asdasdasdasdasd';laskjdf;lkasd;lfkas'ldkf';lasdkf;lkasd;lkajsdflkjasd;lfkja;lsdjkfasdasdasdsdafasdfasdf2"));
        configurationRepository.save(new Configuration("anouncy.password.salt.anonymous", "asdasdasasdfasdfasdflaksdjflkasjhdflkjhasdkljhlaksjdhflkjashdfld3"));
        configurationRepository.save(new Configuration("anouncy.password.salt.session", "asdasdaiouasdpfoiuaspodifuapsoidufpoaisudfipoausdpofiuaspoidufoaiusdpfoiuasd4"));
        configurationRepository.flush();
        configurationService.fetchAllConfigurations();
        jwtTokenProvider.init(); // Reinstall for new configurations
    }

    @Test
    void testAll() throws Exception {
        var userJwtResponse = signUp();
        signIn();
        var userResolveResultDTO = tokenResolver(userJwtResponse);
        signOut(userResolveResultDTO, userJwtResponse);
    }

    UserService.UserJwtResponse signUp() throws Exception {
        UserSignUpDTO validUser = new UserSignUpDTO();

        validUser.setUsername(TEST_USER);
        validUser.setPassword("password");
        validUser.setFirstName("Talha");
        validUser.setLastName("Dilber");
        validUser.setEmail("test@anouncy.com");
        validUser.setImageUrl("imageUrl");
        validUser.setLangKey("tr");
        assertThat(userRepository.findByUsername(TEST_USER)).isEmpty();

        MvcResult mvcResult = mockMvc
                .perform(post("/user/sign-up").contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(validUser)))
                .andExpect(status().isOk()).andReturn();

        UserService.UserJwtResponse resultValue = TestUtil.getResultValue(mvcResult.getResponse().getContentAsString(), UserService.UserJwtResponse.class);

        assertThat(userRepository.findByUsername(TEST_USER)).isPresent();
        assertThat(resultValue.getToken()).isNotBlank();
        return resultValue;
    }


    UserService.UserJwtResponse signIn() throws Exception {
        UserSignInDTO validUser = new UserSignInDTO();

        validUser.setPassword("password");
        validUser.setEmail("test@anouncy.com");
        assertThat(userRepository.findOneByEmailIgnoreCase("test@anouncy.com")).isPresent();

        MvcResult mvcResult = mockMvc
                .perform(post("/user/sign-in").contentType(MediaType.APPLICATION_JSON)
                        .content(TestUtil.convertObjectToJsonBytes(validUser)))
                .andExpect(status().isOk()).andReturn();

        UserService.UserJwtResponse resultValue = TestUtil.getResultValue(mvcResult.getResponse().getContentAsString(), UserService.UserJwtResponse.class);

        assertThat(resultValue.getToken()).isNotBlank();
        return resultValue;
    }

    UserResolveResultDTO tokenResolver(UserService.UserJwtResponse jwtResponse) throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(get("/user/token-resolver/" + jwtResponse.getToken()))
                .andExpect(status().isOk()).andReturn();

        UserResolveResultDTO resultValue = TestUtil.getResultValue(mvcResult.getResponse().getContentAsString(), UserResolveResultDTO.class);

        Optional<User> userOpt = userRepository.findByUsername(TEST_USER);
        assertThat(userOpt).isPresent();
        assertThat(userOpt.get().getId()).isEqualTo(resultValue.getUserId());
        assertThat(resultValue.getAnonymousUserId()).isNotNull();
        return resultValue;
    }

    void signOut(UserResolveResultDTO dto, UserService.UserJwtResponse jwtResponse) throws Exception {
        MvcResult mvcResult = mockMvc
                .perform(post("/user/sign-out")
                        .header("Authorization", jwtResponse.getToken())
                        .header("USER-ID", dto.getUserId())
                        .header("ANONYMOUS-USER-ID", dto.getAnonymousUserId()))
                .andExpect(status().isOk()).andReturn();

        Boolean resultValue = TestUtil.getResultValue(mvcResult.getResponse().getContentAsString(), Boolean.class);

        assertThat(resultValue).isTrue();
    }
}
