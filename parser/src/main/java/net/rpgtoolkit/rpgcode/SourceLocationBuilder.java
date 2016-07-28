package net.rpgtoolkit.rpgcode;

import net.rpgtoolkit.blade.ir.SourceLocation;
import net.rpgtoolkit.blade.ir.SourceRange;

public class SourceLocationBuilder {

  private final Lexer lexer;
  private final LexerContext ctx;
  private SourceLocation start;
  private SourceLocation end;

  public SourceLocationBuilder(Lexer lexer) {
    if (lexer == null)
      throw new IllegalArgumentException();
    this.lexer = lexer;
    this.ctx = lexer.getContext();
  }

  public SourceRange newRange() {
    final SourceRange range = SourceRange.empty();
    start(range);
    return range;
  }

  public void start(SourceRange range) {
    set(range.getStartLocation());
  }

  public void end(SourceRange range) {
    set(range.getEndLocation());
  }

  public void set(SourceLocation location) {
    location.setOffset(ctx.offset);
    location.setLine(ctx.line);
    location.setColumn(ctx.column);
  }

}
