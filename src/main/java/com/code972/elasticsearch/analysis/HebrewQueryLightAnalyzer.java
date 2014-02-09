package com.code972.elasticsearch.analysis;

import org.apache.lucene.analysis.CommonGramsFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.hebrew.HebrewTokenizer;
import org.apache.lucene.analysis.hebrew.StreamLemmasFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;

import java.io.IOException;
import java.io.Reader;

/**
 * Created by synhershko on 12/25/13.
 */
public class HebrewQueryLightAnalyzer extends HebrewAnalyzer {
    public HebrewQueryLightAnalyzer() throws IOException {
        super();
    }

    @Override
    protected TokenStreamComponents createComponents(final String fieldName, final Reader reader) {
        // on query - if marked as keyword don't keep origin, else only lemmatized (don't suffix)
        // if word termintates with $ will output word$, else will output all lemmas or word$ if OOV
        final StreamLemmasFilter src = new StreamLemmasFilter(reader, dictRadix, prefixesTree, SPECIAL_TOKENIZATION_CASES, commonWords, lemmaFilter);
        src.setKeepOriginalWord(false);
        src.setSuffixForExactMatch(originalTermSuffix);

        TokenStream tok = new ASCIIFoldingFilter(src);
        //tok = new SuffixKeywordFilter(tok, '$');
        tok = new AlwaysAddSuffixFilter(tok, '$', true) {
            @Override
            protected boolean possiblySkipFilter() {
                if (HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Hebrew).equals(typeAtt.type())) {
                    if (keywordAtt.isKeyword())
                        termAtt.append(suffix);
                    return true;
                }

                if (HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.NonHebrew).equals(typeAtt.type())) {
                    if (keywordAtt.isKeyword()) {
                        termAtt.append(suffix);
                        return true;
                    }
                }

                if (CommonGramsFilter.GRAM_TYPE.equals(typeAtt.type()) ||
                        HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Numeric).equals(typeAtt.type()) ||
                        HebrewTokenizer.tokenTypeSignature(HebrewTokenizer.TOKEN_TYPES.Mixed).equals(typeAtt.type()))
                {
                    keywordAtt.setKeyword(true);
                    return true;
                }
                return false;
            }
        };
        return new TokenStreamComponents(src, tok);
    }
}