package gov.niem.tools.api.core.config;

import org.apache.lucene.analysis.charfilter.HTMLStripCharFilterFactory;
import org.apache.lucene.analysis.core.LowerCaseFilterFactory;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilterFactory;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.ngram.NGramFilterFactory;
import org.apache.lucene.analysis.snowball.SnowballPorterFilterFactory;
import org.apache.lucene.analysis.standard.StandardTokenizerFactory;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurationContext;
import org.hibernate.search.backend.lucene.analysis.LuceneAnalysisConfigurer;

public class AppLuceneAnalysisConfigurer implements LuceneAnalysisConfigurer {

  @Override
  public void configure(LuceneAnalysisConfigurationContext context) {
    context.analyzer("camel").custom()
        .tokenizer(StandardTokenizerFactory.class)
        .charFilter(HTMLStripCharFilterFactory.class)
        .tokenFilter(WordDelimiterGraphFilterFactory.class)
        .tokenFilter(LowerCaseFilterFactory.class)
        // .param("language", "English")
        .tokenFilter(SnowballPorterFilterFactory.class)
        .tokenFilter(ASCIIFoldingFilterFactory.class);

    context.analyzer("substring").custom()
        .tokenizer(StandardTokenizerFactory.class)
        .charFilter(HTMLStripCharFilterFactory.class)
        .tokenFilter(LowerCaseFilterFactory.class)
        // .param("language", "English")
        // .tokenFilter(SnowballPorterFilterFactory.class)
        .tokenFilter(ASCIIFoldingFilterFactory.class)
        // .tokenFilter(EdgeNGramFilterFactory.class)
        .tokenFilter(NGramFilterFactory.class)
          .param("minGramSize", "3")
          .param("maxGramSize", "10");

  }

}
