package demo

import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros
import scala.annotation.StaticAnnotation
import scala.reflect.runtime.universe._

import scala.annotation.compileTimeOnly

@compileTimeOnly("@demo.free not expanded")
class free extends scala.annotation.StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro freeMacroImpl.impl
}
class freeMacroImpl(val c: Context) {
  import c.universe._
  
  def impl(annottees: c.Expr[Any]*): c.Expr[Any] = {
    
    val r = annottees.map(_.tree) match {
        case q"$mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self => ..$stats }" :: Nil => {
          val free = freeObject(mods, tpname, tparams, earlydefns, parents, self, stats)

          c.Expr(q"""
            $mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self => ..$stats }
            object ${tpname.toTermName}{
              ..$free
            }
          """)
        }
        case q"$mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self => ..$stats }" ::
          q"$omods object $otname extends { ..$oearlydefns } with ..$oparents { $oself => ..$obody }" :: Nil => {
          val free = freeObject(mods, tpname, tparams, earlydefns, parents, self, stats)

          c.Expr(q"""
          $mods trait $tpname[..$tparams] extends { ..$earlydefns } with ..$parents { $self => ..$stats }
          $omods object $otname extends { ..$oearlydefns } with ..$oparents { 
            $oself => ..$obody
            ..$free
          }
        """)
        }

        case a => c.abort(c.enclosingPosition, "Macro failed to match!")
      }
      println(r)
      r
    }     

    def freeObject(mods: Modifiers, tpname: TypeName, tparams: List[TypeDef], earlydefns: List[Tree], parents: List[Tree], self: ValDef, stats: List[Tree]) = {
      q"""
        object data {
          sealed trait Op[T]
          ..${for (param <- stats.collect{case d:DefDef=>d}) yield operationDatum(param)}
        }
        type OpM[T] = cats.free.Free[data.Op, T]
        ..${for (param <- stats.collect{case d:DefDef=>d}) yield operationLifted(param)}
        
        import scala.language.higherKinds
        trait HighKinded[M[_]]{
          ..${for (param <- stats.collect{case d:DefDef=>d}) yield operationHighKinded(param)}
        }
      """
    }
    
    def datumName(term: TermName) = term.toString().charAt(0).toUpper+term.toString().substring(1)
    def datumType(term: TermName) = TypeName(datumName(term))

    def operationDatum(method: DefDef) = q"""
      case class ${TypeName(datumName(method.name))}(...${method.vparamss}) extends Op[${method.tpt}]
    """
      
    def operationLifted(method: DefDef) = q"""
      def ${method.name}(...${method.vparamss}): OpM[${method.tpt}] = cats.free.Free.liftF(data.${TermName(datumName(method.name))}(...${method.vparamss.map(_.map(_.name))}))
    """
    
    def operationHighKinded(method: DefDef) = q"""
      def ${method.name}(...${method.vparamss}): M[${method.tpt}]
    """

  }