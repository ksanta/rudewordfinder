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

    private List<String> rudeWords;

    public RudeWordFinder() {
        try {
            Path path = Paths.get(getClass().getClassLoader().getResource("rude_word_list.txt").toURI());
            Stream<String> lines = Files.lines(path).map(s -> s.replace(" ", ""));
            rudeWords = lines.collect(Collectors.toList());
            lines.close();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    // This is the driver method
    public List<String> find(List<String> inputWords) {
        List<String> results = new ArrayList<>();
        
        inputWords = splitSpacesFromInputWords(inputWords);

        for (String rudeWord : rudeWords) {
            // match() will mangle inputWords, so send in a fresh copy of it every time
            String match = match(rudeWord, new ArrayList<>(inputWords));
            if (match != null) {
                results.add(match);
            }
        }

        return results;
    }

    /**
     * This will search input words for spaces and split them into separate words
     */
    private List<String> splitSpacesFromInputWords(List<String> inputWords) {
        List<String> splitWords = new ArrayList<>();
        for (String inputWord : inputWords) {
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
        // Try and find a match from the whole word, then try shorter and short lengths, scanning across the word
        for (int rudeWordSearchLength = rudeWord.length(); rudeWordSearchLength >= 1; rudeWordSearchLength--) {
            int startIndex = 0;
            while (startIndex + rudeWordSearchLength <= rudeWord.length()) {
                String rudeWordFragment = rudeWord.substring(startIndex, startIndex + rudeWordSearchLength);

                boolean foundMatch = scanInputWordsForMatch(inputWords, rudeWordFragment);

                if (foundMatch) {
                    StringBuilder sb = new StringBuilder();

                    // Recursive call for any portions of the rude word not covered by the rude word fragment
                    if (startIndex > 0) {
                        String prefixMatches = match(rudeWord.substring(0, startIndex), inputWords);
                        if (prefixMatches == null) {
                            startIndex++;
                            continue;
                        }
                        sb.append(prefixMatches).append("+");
                    }

                    sb.append(rudeWordFragment);

                    if (startIndex + rudeWordSearchLength < rudeWord.length()) {
                        String suffixMatches = match(rudeWord.substring(startIndex + rudeWordSearchLength), inputWords);
                        if (suffixMatches == null) {
                            startIndex++;
                            continue;
                        }
                        sb.append("+").append(suffixMatches);
                    }

                    return sb.toString();
                }

                startIndex++;
            }
        }

        return null;
    }

    private boolean scanInputWordsForMatch(List<String> inputWords, String rudeWordFragment) {
        for (String inputWord : inputWords) {
            if (inputWord.contains(rudeWordFragment)) {
                updateInputWords(inputWords, rudeWordFragment, inputWord);
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
