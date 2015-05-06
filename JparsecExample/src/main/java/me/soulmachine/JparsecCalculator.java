package me.soulmachine;

import org.codehaus.jparsec.OperatorTable;
import org.codehaus.jparsec.Parser;
import org.codehaus.jparsec.Parsers;
import org.codehaus.jparsec.Scanners;
import org.codehaus.jparsec.Terminals;
import org.codehaus.jparsec.functors.Binary;
import org.codehaus.jparsec.functors.Map;
import org.codehaus.jparsec.functors.Unary;


/**
 * An arithmetic caculator.
 *
 * @see <a href=https://github.com/jparsec/jparsec/wiki/Tutorial>Calculator</a>
 */
@SuppressWarnings({"PMD.ShortVariable", "PMD.CommentRequired","PMD.ShortMethodName"})
public final class JparsecCalculator {

  private static final Terminals OPERATORS = Terminals.operators("+", "-", "*", "/", "(", ")");

  private static final Parser<Void> IGNORED =
      Parsers.or(Scanners.JAVA_LINE_COMMENT, Scanners.JAVA_BLOCK_COMMENT, Scanners.WHITESPACES)
          .skipMany();

  private static final Parser<?> TOKENIZER =
      Parsers.or(Terminals.DecimalLiteral.TOKENIZER, OPERATORS.tokenizer());

  private static final Parser<BinaryOperator> WHITESPACE_MUL =
      term("+", "-", "*", "/").not().retn(BinaryOperator.MUL);

  private static final Parser<Double> NUMBER =
      Terminals.DecimalLiteral.PARSER.map(new Map<String, Double>() {
        public Double map(final String s) {
          return Double.valueOf(s);
        }
      });

  @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
  public static final Parser<Double> CALCULATOR = calculator(NUMBER).from(TOKENIZER, IGNORED);

  private JparsecCalculator() {}

  /** Arithmetic Operators. */
  enum BinaryOperator implements Binary<Double> {
    PLUS {
      public Double map(final Double a, final Double b) {
        return a + b;
      }
    },
    MINUS {
      public Double map(final Double a, final Double b) {
        return a - b;
      }
    },
    MUL {
      public Double map(final Double a, final Double b) {
        return a * b;
      }
    },
    DIV {
      public Double map(final Double a, final Double b) {
        return a / b;
      }
    }
  }

  /** Negative Operators. */
  enum UnaryOperator implements Unary<Double> {
    NEG {
      public Double map(final Double n) {
        return -n;
      }
    }
  }

  private static Parser<?> term(final String... names) {
    return OPERATORS.token(names);
  }

  private static <T> Parser<T> op(final String name, final T value) {
    return term(name).retn(value);
  }

  private static Parser<Double> calculator(final Parser<Double> atom) {
    final Parser.Reference<Double> ref = Parser.newReference();
    final Parser<Double> unit = ref.lazy().between(term("("), term(")")).or(atom);
    final Parser<Double> parser =
        new OperatorTable<Double>().infixl(op("+", BinaryOperator.PLUS), 10)
        .infixl(op("-", BinaryOperator.MINUS), 10)
        .infixl(op("*", BinaryOperator.MUL).or(WHITESPACE_MUL), 20)
        .infixl(op("/", BinaryOperator.DIV), 20).prefix(op("-", UnaryOperator.NEG), 30).build(unit);
    ref.set(parser);
    return parser;
  }

  /**
   * Main function.
   *
   * @param args arguments
   */
  public static void main(final String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: JparsecCalculator expr");
      return;
    }

    final Double result = CALCULATOR.parse(args[0]);
    System.out.println(result);
  }
}
