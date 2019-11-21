package lol.karl.rudewordfinder;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@Slf4j
public class RudeWordFinderTest {

    @Test
    public void testRudeWordsLoadCorrectly() {
        RudeWordFinder finder = new RudeWordFinder();
        assertThat(finder.getRudeWords().isEmpty(), is(false));
    }

    @Test
    public void testNoMatches() {
        List<String> inputWords = Collections.singletonList("mars");

        RudeWordFinder finder = new RudeWordFinder();
        List<String> results = finder.find(inputWords);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testDirectMatch() {
        List<String> inputWords = Collections.singletonList("sex");

        RudeWordFinder finder = new RudeWordFinder();
        List<String> results = finder.find(inputWords);
        log.debug("Input {} contains rude words {}", inputWords.toString(), results.toString());
        assertTrue(results.contains("sex"));
    }

    @Test
    public void testTwoMatch() {
        List<String> inputWords = Arrays.asList("an", "al");

        RudeWordFinder finder = new RudeWordFinder();
        List<String> results = finder.find(inputWords);
        log.debug("Input {} contains rude words {}", inputWords.toString(), results.toString());
        assertTrue(results.contains("an+al"));
    }

    @Test
    public void testTwoMatchFromParts() {
        List<String> inputWords = Arrays.asList("sultan", "all");

        RudeWordFinder finder = new RudeWordFinder();
        List<String> results = finder.find(inputWords);
        log.debug("Input {} contains rude words {}", inputWords.toString(), results.toString());
        assertTrue(results.contains("an+al"));
    }

    @Test
    public void testLargeMatch() {
        List<String> inputWords = Arrays.asList("finger", "ring");

        RudeWordFinder finder = new RudeWordFinder();
        List<String> results = finder.find(inputWords);
        assertTrue(results.contains("finger+ing"));
        log.debug("Input {} contains rude words {}", inputWords.toString(), results.toString());
        assertTrue(results.contains("n+i+g+ger"));
    }

    @Test
    public void testMatchResultsWithSpaces() {
        List<String> inputWords = Arrays.asList("foot", "fetish");

        RudeWordFinder finder = new RudeWordFinder();
        List<String> results = finder.find(inputWords);
        log.debug("Input {} contains rude words {}", inputWords.toString(), results.toString());
        assertTrue(results.contains("foot+fetish"));
    }

    @Test
    public void testDoesNotUseInputTwice() {
        List<String> inputWords = Arrays.asList("finger", "sit");

        RudeWordFinder finder = new RudeWordFinder();
        List<String> results = finder.find(inputWords);
        log.debug("Input {} contains rude words {}", inputWords.toString(), results.toString());
        assertFalse(results.contains("n+i+g+ger"));
    }
}
