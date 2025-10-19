package com.mytutorplatform.lessonsservice.service;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishPossessiveFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringReader;
import java.util.*;

@Service
public class EnglishCoverageEngine {

  private static final Set<String> PHRASAL_PARTICLES = Set.of(
          "up","off","out","in","into","on","over","away","back","down","through","around","after","for","from"
  );

  private final Analyzer analyzer;
  private final int phrasalMaxGap;

  public EnglishCoverageEngine() {
    this.phrasalMaxGap = 3;
    this.analyzer = new Analyzer() {
      @Override
      protected TokenStreamComponents createComponents(String fieldName) {
        Tokenizer src = new StandardTokenizer();
        TokenStream ts = new EnglishPossessiveFilter(src); // John's -> John
        ts = new LowerCaseFilter(ts);
        ts = new KStemFilter(ts);
        return new TokenStreamComponents(src, ts);
      }
    };
  }

  public Map<String, Boolean> computeCoverage(String transcript, List<String> targetItems) {
    var tokens = analyzeWithOffsets(defaultString(transcript));
    Map<String, List<Integer>> positionsByLemma = indexPositions(tokens);

    Map<String, Boolean> result = new LinkedHashMap<>();
    for (String originalTarget : targetItems) {
      boolean present = matchTarget(originalTarget, tokens, positionsByLemma);
      result.put(originalTarget, present);
    }
    return result;
  }

  private boolean matchTarget(String originalTarget,
                              List<Token> tokens,
                              Map<String, List<Integer>> index) {

    List<String> lemmas = analyzeToLemmas(cleanTarget(originalTarget));
    if (lemmas.isEmpty()) return false;

    lemmas = stripLeadingTo(lemmas);

    if (lemmas.size() == 1) {
      String lemma = lemmas.get(0);
      return index.containsKey(lemma);
    }

    if (matchContiguousPhrase(lemmas, tokens)) return true;

    if (lemmas.size() == 2 && PHRASAL_PARTICLES.contains(lemmas.get(1))) {
      return matchPhrasalWithGap(lemmas.get(0), lemmas.get(1), tokens, phrasalMaxGap);
    }

    return false;
  }

  private static List<String> stripLeadingTo(List<String> lemmas) {
    if (!lemmas.isEmpty() && "to".equals(lemmas.get(0)) && lemmas.size() >= 2) {
      return lemmas.subList(1, lemmas.size());
    }
    return lemmas;
  }

  private boolean matchContiguousPhrase(List<String> phraseLemmas, List<Token> tokens) {
    if (phraseLemmas.size() > tokens.size()) return false;
    for (int i = 0; i <= tokens.size() - phraseLemmas.size(); i++) {
      boolean ok = true;
      for (int j = 0; j < phraseLemmas.size(); j++) {
        if (!tokens.get(i + j).lemma.equals(phraseLemmas.get(j))) {
          ok = false; break;
        }
      }
      if (ok) return true;
    }
    return false;
  }

  private boolean matchPhrasalWithGap(String verbLemma, String particle, List<Token> tokens, int maxGap) {
    List<Integer> verbPos = new ArrayList<>();
    for (int i = 0; i < tokens.size(); i++) {
      if (verbLemma.equals(tokens.get(i).lemma)) verbPos.add(i);
    }
    if (verbPos.isEmpty()) return false;

    for (int v : verbPos) {
      int start = Math.min(tokens.size() - 1, v + 1);
      int end = Math.min(tokens.size() - 1, v + 1 + maxGap);
      for (int i = start; i <= end; i++) {
        if (particle.equals(tokens.get(i).lemma)) return true;
      }
    }
    return false;
  }

  // ==== Lucene helpers ====

  private List<Token> analyzeWithOffsets(String text) {
    List<Token> out = new ArrayList<>();
    try (TokenStream ts = analyzer.tokenStream("f", new StringReader(text))) {
      CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
      OffsetAttribute off = ts.addAttribute(OffsetAttribute.class);
      ts.reset();
      int idx = 0;
      while (ts.incrementToken()) {
        out.add(new Token(term.toString(), off.startOffset(), off.endOffset(), idx++));
      }
      ts.end();
    } catch (IOException ignored) {
    }
    return out;
  }

  private List<String> analyzeToLemmas(String text) {
    List<String> out = new ArrayList<>();
    try (TokenStream ts = analyzer.tokenStream("f", new StringReader(text))) {
      CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
      ts.reset();
      while (ts.incrementToken()) {
        out.add(term.toString());
      }
      ts.end();
    } catch (IOException e) {
      // ignore
    }
    return out;
  }

  private Map<String, List<Integer>> indexPositions(List<Token> tokens) {
    Map<String, List<Integer>> map = new HashMap<>();
    for (Token t : tokens) {
      map.computeIfAbsent(t.lemma, k -> new ArrayList<>()).add(t.index);
    }
    return map;
  }

  private static String cleanTarget(String s) {
    return defaultString(s).trim();
  }

  private static String defaultString(String s) {
    return s == null ? "" : s;
  }

  private static final class Token {
    final String lemma;
    final int start, end, index;
    Token(String lemma, int start, int end, int index) {
      this.lemma = lemma; this.start = start; this.end = end; this.index = index;
    }
  }
}
