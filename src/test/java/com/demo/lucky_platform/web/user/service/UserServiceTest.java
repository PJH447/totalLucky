package com.demo.lucky_platform.web.user.service;

import com.demo.lucky_platform.web.user.domain.PhoneCertification;
import com.demo.lucky_platform.web.user.domain.Role;
import com.demo.lucky_platform.web.user.domain.User;
import com.demo.lucky_platform.web.user.dto.SignUpForm;
import com.demo.lucky_platform.web.user.repository.PhoneCertificationRepository;
import com.demo.lucky_platform.web.user.repository.RoleRepository;
import com.demo.lucky_platform.web.user.repository.UserRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.response.Certification;
import com.siot.IamportRestClient.response.IamportResponse;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ActiveProfiles({"test"})
@Transactional
@SpringBootTest
class UserServiceTest {

    @Autowired
    UserService userService;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
    @MockBean
    UserRepository userRepository;
    @MockBean
    RoleRepository roleRepository;
    @MockBean
    IamportClient iamportClient;
    @MockBean
    PhoneCertificationRepository phoneCertificationRepository;

    @Mock
    User user;
    @Mock
    Role role;
    @Mock
    IamportResponse<Certification> iamportResponse;
    @Mock
    Certification certification;
    @Mock
    IamportResponseException iamportResponseException;
    @Mock
    PhoneCertification phoneCertification;

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTest {

        SignUpForm signUpForm;
        String impUid = "impUid";

        @BeforeEach
        void setup() {
            signUpForm = SignUpForm.builder()
                                   .name("name")
                                   .nickname("nickname")
                                   .email("email@naver.com")
                                   .password("password")
                                   .impUid(impUid)
                                   .build();
        }

        @Test
        void 성공() throws IamportResponseException, IOException {

            //given
            doReturn(user).when(userRepository).save(any());
            doReturn(role).when(roleRepository).findByAuthority(any());
            doReturn(iamportResponse).when(iamportClient).certificationByImpUid(any());
            doReturn(certification).when(iamportResponse).getResponse();
            doReturn(impUid).when(certification).getImpUid();
            doReturn("uniqueKey").when(certification).getUniqueKey();
            doReturn(Optional.empty()).when(phoneCertificationRepository).findByUniqueKeyAndEnabledIsTrue(any());

            //when
            userService.signUp(signUpForm);

            //then
            verify(userRepository, times(1)).save(any());
            verify(phoneCertificationRepository, times(1)).save(any());
        }

        @ParameterizedTest(name = "{index} : {1}")
        @MethodSource("errorCodeStream")
        @DisplayName("실패 테스트")
        void 실패1(int errorCode, String message) throws IamportResponseException, IOException {

            //given
            doReturn(user).when(userRepository).save(any());
            doReturn(role).when(roleRepository).findByAuthority(any());
            doThrow(iamportResponseException).when(iamportClient).certificationByImpUid(any());
            doReturn("error!!!").when(iamportResponseException).getMessage();

            //when
            doReturn(errorCode).when(iamportResponseException).getHttpStatusCode();

            //then
            assertThrows(RuntimeException.class, () -> userService.signUp(signUpForm));
        }

        private static Stream<Arguments> errorCodeStream() {
            return Stream.of(
                    Arguments.of(401, "import error code = 401"),
                    Arguments.of(404, "import error code = 404"),
                    Arguments.of(500, "import error code = 500")
            );
        }

        @Test
        @DisplayName("iamport IOException")
        void 실패2() throws IamportResponseException, IOException {

            //given
            doReturn(user).when(userRepository).save(any());
            doReturn(role).when(roleRepository).findByAuthority(any());

            //when
            doThrow(IOException.class).when(iamportClient).certificationByImpUid(any());

            //then
            assertThrows(RuntimeException.class, () -> userService.signUp(signUpForm));
        }

        @Test
        @DisplayName("impUid is different")
        void 실패3() throws IamportResponseException, IOException {

            //given
            doReturn(user).when(userRepository).save(any());
            doReturn(role).when(roleRepository).findByAuthority(any());
            doReturn(iamportResponse).when(iamportClient).certificationByImpUid(any());
            doReturn(certification).when(iamportResponse).getResponse();

            //when
            doReturn("impUid2").when(certification).getImpUid();

            //then
            assertThrows(RuntimeException.class, () -> userService.signUp(signUpForm));
        }

        @Test
        @DisplayName("exist certification log")
        void 실패4() throws IamportResponseException, IOException {

            //given
            doReturn(user).when(userRepository).save(any());
            doReturn(role).when(roleRepository).findByAuthority(any());
            doReturn(iamportResponse).when(iamportClient).certificationByImpUid(any());
            doReturn(certification).when(iamportResponse).getResponse();
            doReturn(impUid).when(certification).getImpUid();
            doReturn("uniqueKey").when(certification).getUniqueKey();

            //when
            doReturn(Optional.ofNullable(phoneCertification)).when(phoneCertificationRepository).findByUniqueKeyAndEnabledIsTrue(any());

            //then
            assertThrows(RuntimeException.class, () -> userService.signUp(signUpForm));
        }
    }

    @Nested
    @DisplayName("닉네임 변경 테스트")
    class EditNicknameTest {

        @Test
        @DisplayName("성공 테스트")
        void 성공() {

            //given
            doReturn(Optional.empty()).when(userRepository).findByNicknameAndEnabledIsTrue(any());
            doReturn(Optional.ofNullable(user)).when(userRepository).findById(any());

            //when
            userService.editNickname(1L, "newNickname");

            //then
            verify(user, times(1)).editNickname(any());
            verify(userRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("이미 동일 닉네임 유저가 존재")
        void 실패1() {

            //given

            //when
            doReturn(Optional.ofNullable(user)).when(userRepository).findByNicknameAndEnabledIsTrue(any());

            //then
            assertThrows(RuntimeException.class, () -> userService.editNickname(1L, "newNickname"));
        }

        @Test
        @DisplayName("바꾸고자하는 대상이 존재하지 않음")
        void 실패2() {

            //given
            doReturn(Optional.empty()).when(userRepository).findByNicknameAndEnabledIsTrue(any());

            //when
            doReturn(Optional.empty()).when(userRepository).findById(any());

            //then
            assertThrows(RuntimeException.class, () -> userService.editNickname(1L, "newNickname"));
        }

    }
}