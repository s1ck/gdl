/*
 * [The "BSD license"]
 *  Copyright (c) 2012 Terence Parr
 *  Copyright (c) 2012 Sam Harwell
 *  All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions
 *  are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *  2. Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *  3. The name of the author may not be used to endorse or promote products
 *     derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 *  IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 *  OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 *  IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 *  INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 *  NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 *  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 *  THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 *  THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.s1ck.gdl.exceptions;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.ParseCancellationException;

/**
 * This ErrorStrategy throws an exception if the syntax is not matching the expected GDL language, but in
 * addition to the BailErrorStrategy also prints out the first syntax mismatch
 */
public class BailSyntaxErrorStrategy extends DefaultErrorStrategy {
  /**
   *  Instead of recovering from exception {@code e}, re-throw it wrapped
   *  in a {@link ParseCancellationException} so it is not caught by the
   *  rule function catches.  Use {@link Exception#getCause()} to get the
   *  original {@link RecognitionException}. To print the syntax error the
   *  {@link DefaultErrorStrategy#recover(Parser, RecognitionException)} method
   *  gets executed.
   */
  @Override
  public void recover(Parser recognizer, RecognitionException e) {
    super.recover(recognizer, e);
    for (ParserRuleContext context = recognizer.getContext(); context != null; context = context.getParent()) {
      context.exception = e;
    }
    throw new ParseCancellationException(e);
  }

  /**
   *  Make sure we don't attempt to recover inline; if the parser
   *  successfully recovers, it won't throw an exception.
   *  Again, the {@link DefaultErrorStrategy#recoverInline(Parser)} gets executed
   *  to print the wrong syntax
   */
  @Override
  public Token recoverInline(Parser recognizer) throws RecognitionException {
    super.recoverInline(recognizer);
    InputMismatchException e = new InputMismatchException(recognizer);
    for (ParserRuleContext context = recognizer.getContext(); context != null; context = context.getParent()) {
      context.exception = e;
    }
    throw new ParseCancellationException(e);
  }

  /**
   * Make sure we don't attempt to recover from problems in subrules.
   */
  @Override
  public void sync(Parser recognizer) { }
}
