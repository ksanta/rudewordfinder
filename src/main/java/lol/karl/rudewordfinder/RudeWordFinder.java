package lol.karl.rudewordfinder;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Getter
public class RudeWordFinder {

    private static final String SNIP_CHARACTER = "|";

    private List<String> rudeWords;

    public RudeWordFinder() {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource("rude_word_list.txt").toURI());
            Stream<String> lines = Files.lines(path)
                    .map(s -> s.replace(" ", ""))
                    .map(String::toLowerCase);
            rudeWords = lines.collect(Collectors.toList());
            lines.close();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // This is the driver method
    public List<String> find(List<String> inputWords) {
        List<String> results = new ArrayList<>();
        
        List<String> cleanedInputWords = cleanInputWords(inputWords);

        for (String rudeWord : rudeWords) {
            // match() will mangle inputWords, so send in a fresh copy of it every time
            String match = match(rudeWord, new ArrayList<>(cleanedInputWords));
            if (match != null) {
                results.add(match);
            }
        }

        // Apply ordering: move longer running fragments to the front, single character builds to the back
        results.sort(Comparator.comparingInt(this::longestFragmentLength).reversed());

        return results;
    }

    private int longestFragmentLength(String word) {
        int longestLength = 0;
        for (String inputWordFragment : word.split("\\"+ SNIP_CHARACTER)) {
            if (inputWordFragment.length() > longestLength) {
                longestLength = inputWordFragment.length();
            }
        }
        return longestLength;
    }

    /**
     * This will search input words for spaces and split them into separate words.  This will lowercase all the input
     * words.
     */
    private List<String> cleanInputWords(List<String> inputWords) {
        List<String> splitWords = new ArrayList<>();
        for (String inputWord : inputWords) {
            inputWord = inputWord.toLowerCase();
            if (inputWord.contains(" ")) {
                splitWords.addAll(Arrays.asList(inputWord.split(" ")));
            } else {
                splitWords.add(inputWord);
            }
        }
        return splitWords;
    }

    /**
     * Recursively checks the given rude word (or portion thereof) to see if can be built up from the list of input
     * words (or input word fragments)
     *
     * @param rudeWord   the word which we are trying to build, or a portion of the initial word when we recur
     * @param inputWords list of given input words we have to work with, or chopped up fragments when we recur
     * @return a string representing how to build up the target word, or null if it cannot be built
     */
    private String match(String rudeWord, List<String> inputWords) {
        // Define the length of the sliding window, going from whole word legnth to single character
        for (int rudeWordWindowLength = rudeWord.length(); rudeWordWindowLength >= 1; rudeWordWindowLength--) {
            // Slide the window across the rude word
            for (int startIndex = 0; startIndex + rudeWordWindowLength <= rudeWord.length(); startIndex++ ) {
                String rudeWordBuiltFromInputWords = findMatchesForRudeWordWindow(rudeWord, startIndex, rudeWordWindowLength, inputWords);
                if (rudeWordBuiltFromInputWords != null) {
                    return rudeWordBuiltFromInputWords;
                }
            }
        }
        // Rude word has no matches
        return null;
    }

    private String findMatchesForRudeWordWindow(String rudeWord, int windowStartIndex, int windowLength, List<String> inputWords) {
        String windowContent = rudeWord.substring(windowStartIndex, windowStartIndex + windowLength);

        boolean matchFound = matchAndSnipInputWords(inputWords, windowContent);

        if (!matchFound) {
            return null;
        }

        StringBuilder rudeWordBuiltFromInputs = new StringBuilder();

        // Recursive call for any portions of the rude word not covered by the rude word fragment
        if (windowStartIndex > 0) {
            String fragmentBeforeWindow = match(rudeWord.substring(0, windowStartIndex), inputWords);
            if (fragmentBeforeWindow == null) {
                return null;
            }
            rudeWordBuiltFromInputs.append(fragmentBeforeWindow).append(SNIP_CHARACTER);
        }

        rudeWordBuiltFromInputs.append(windowContent);

        if (windowStartIndex + windowLength < rudeWord.length()) {
            String fragmentAfterWindow = match(rudeWord.substring(windowStartIndex + windowLength), inputWords);
            if (fragmentAfterWindow == null) {
                return null;
            }
            rudeWordBuiltFromInputs.append(SNIP_CHARACTER).append(fragmentAfterWindow);
        }

        return rudeWordBuiltFromInputs.toString();
    }

    /**
     * Searches input words for the given fragment. If found, will remove the fragment portion from the input word and
     * return true
     * @return true if a match was made and the input words list was updated to remove the matched portion
     */
    private boolean matchAndSnipInputWords(List<String> inputWords, String fragmentToMatch) {
        for (String inputWord : inputWords) {
            if (inputWord.contains(fragmentToMatch)) {
                updateInputWords(inputWords, fragmentToMatch, inputWord);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes inputWord from inputWords, and adds in any portion of inputWord that isn't taken up by rudeWordFragment.
     */
    private void updateInputWords(List<String> inputWords, String rudeWordFragment, String inputWord) {
        // Remove the input word and add in any parts of the input word that don't match
        inputWords.remove(inputWord);
        if (rudeWordFragment.length() < inputWord.length()) {
            int matchStartIndex = inputWord.indexOf(rudeWordFragment);
            // Check for fragment at the start
            if (matchStartIndex > 0) {
                inputWords.add(inputWord.substring(0, matchStartIndex));
            }
            // Check for fragment at the end
            if (matchStartIndex + rudeWordFragment.length() < inputWord.length()) {
                inputWords.add(inputWord.substring(matchStartIndex + rudeWordFragment.length()));
            }
        }
    }
}
