package com.mytutorplatform.lessonsservice.service;

import org.junit.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnglishCoverageEngineTest {
  EnglishCoverageEngine engine = new EnglishCoverageEngine();

  @Test
  public void singleWord_irregularVerb() {
    var m = engine.computeCoverage("He ran every day and slept well.", List.of("run", "sleep"));
    assertTrue(m.get("run"));
    assertTrue(m.get("sleep"));
  }

  @Test
  public void toInfinitive_ok() {
    var m = engine.computeCoverage("Running is healthy.", List.of("to run"));
    assertTrue(m.get("to run"));
  }

  @Test
  public void phrasalVerb_contiguous() {
    var m = engine.computeCoverage("Please look up this word.", List.of("look up"));
    assertTrue(m.get("look up"));
  }

  @Test
  public void phrasalVerb_splitObject() {
    var m = engine.computeCoverage("Please look this word up for me.", List.of("look up"));
    assertTrue(m.get("look up"));
  }

  @Test
  public void phrase_contiguous_words() {
    var m = engine.computeCoverage("He has a big red car.", List.of("big red"));
    assertTrue(m.get("big red"));
  }
}