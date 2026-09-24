package pm.repos;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RemoteUrl")
class RemoteUrlTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "https://github.com/octo-user/ProjectManager.git",
            "https://github.com/octo-user/ProjectManager",
            "https://github.com/octo-user/ProjectManager/",
            "http://github.com/octo-user/ProjectManager.git",
            "git@github.com:octo-user/ProjectManager.git",
            "git@github.com:octo-user/ProjectManager",
            "ssh://git@github.com/octo-user/ProjectManager.git",
            "ssh://git@github.com:22/octo-user/ProjectManager.git",
            "git://github.com/octo-user/ProjectManager.git",
            "https://x-access-token:secret123@github.com/octo-user/ProjectManager.git",
            "https://user:p@ss@github.com/octo-user/ProjectManager.git",
            "  https://github.com/octo-user/ProjectManager.git  "
    })
    @DisplayName("normalizes every GitHub URL form to owner/name")
    void normalizesForms(String url) {
        assertEquals(Optional.of("octo-user/projectmanager"), RemoteUrl.githubKey(url));
    }

    @Test
    @DisplayName("case is normalized")
    void caseIsNormalized() {
        assertEquals(RemoteUrl.githubKey("https://GitHub.com/Octo-User/PROJECTMANAGER.git"),
                Optional.of(RemoteUrl.key("octo-user", "ProjectManager")));
    }

    @Test
    @DisplayName("names with dots keep their dots")
    void namesWithDotsKeepTheirDots() {
        assertEquals(Optional.of("other-user/hyperionsmc.com"),
                RemoteUrl.githubKey("https://github.com/other-user/hyperionsmc.com.git"));
        assertEquals(Optional.of("other-user/hyperionsmc.com"),
                RemoteUrl.githubKey("git@github.com:other-user/hyperionsmc.com"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "https://gitlab.com/octo-user/project.git",
            "https://github.com.evil.example/octo-user/project.git",
            "https://evil.example/github.com/octo-user/project.git",
            "https://github.com/octo-user",
            "https://github.com/octo-user/project/extra",
            "not a url",
            ""
    })
    @DisplayName("non-GitHub or malformed URLs have no key")
    void rejectsOthers(String url) {
        assertTrue(RemoteUrl.githubKey(url).isEmpty());
    }

    @Test
    @DisplayName("null has no key")
    void nullHasNoKey() {
        assertTrue(RemoteUrl.githubKey(null).isEmpty());
    }

    @Test
    @DisplayName("redact removes embedded credentials")
    void redactRemovesCredentials() {
        assertEquals("https://github.com/o/r.git",
                RemoteUrl.redact("https://x-access-token:ghp_secret@github.com/o/r.git"));
        assertEquals("https://gitlab.example/o/r.git",
                RemoteUrl.redact("https://user:pass@gitlab.example/o/r.git"));
    }

    @Test
    @DisplayName("redact removes userinfo up to the last @, even with @ or / in the password")
    void redactRemovesAllUserinfo() {
        assertEquals("https://github.com/o/r.git", RemoteUrl.redact("https://user:p@ss@github.com/o/r.git"));
        String slashed = RemoteUrl.redact("https://user:pa/ss@host/o/r");
        assertFalse(slashed.contains("pa"), slashed);
        assertFalse(slashed.contains("ss@"), slashed);
        assertFalse(slashed.contains("user"), slashed);
        assertTrue(slashed.startsWith("https://"), slashed);
    }

    @Test
    @DisplayName("redact leaves URLs without credentials alone")
    void redactKeepsPlainUrls() {
        assertEquals("git@github.com:o/r.git", RemoteUrl.redact("git@github.com:o/r.git"));
        assertEquals("https://github.com/o/r", RemoteUrl.redact("https://github.com/o/r"));
        assertEquals("", RemoteUrl.redact(null));
    }
}
