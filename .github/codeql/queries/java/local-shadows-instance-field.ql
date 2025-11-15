/**
 * @name Local variable shadows instance field (except final this-copy)
 * @description Flags local variables that shadow an instance field, unless they are final
 *              and initialized directly from that same field (for example `final var name = this.name;`).
 * @id custom-java/local-shadows-instance-field-strict
 * @kind problem
 * @problem.severity warning
 * @precision medium
 * @tags correctness readability maintainability
 */
import java
import semmle.code.java.Member
import semmle.code.java.Variable

/**
 * True if this local variable is an allowed shadowing of the given instance field:
 *
 *   final var name = this.name;
 *   final var name = name;       // implicit this
 */
predicate allowedShadowing(LocalVariableDecl local, Field field) {
  local.isFinal() and
  exists(FieldAccess fa |
    fa = local.getInitializer().getUnderlyingExpr().(FieldAccess) and
    fa.getField() = field and
    fa.isOwnFieldAccess()
  )
}

from LocalVariableDecl local, Field field, Callable c
where
  // same simple name
  local.getName() = field.getName() and

  // only consider instance fields
  not field.isStatic() and

  // local is inside a callable of a class related to the field's declaring type
  c = local.getCallable() and
  c.getDeclaringType().getASupertype*() = field.getDeclaringType() and

  // shadowing is NOT in the allowed final-this-copy form
  not allowedShadowing(local, field)
select local,
  "Local variable '" + local.getName() + "' shadows instance field '" +
  field.getQualifiedName() +
  "'. Shadowing is only allowed for 'final' locals directly initialized from this field."
