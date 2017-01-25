/*
 * Copyright 1999-2101 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.druid.sql.dialect.sqlserver.visitor;

import com.alibaba.druid.sql.ast.SQLSetQuantifier;
import com.alibaba.druid.sql.ast.statement.SQLExprTableSource;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerOutput;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelect;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerSelectQueryBlock;
import com.alibaba.druid.sql.dialect.sqlserver.ast.SQLServerTop;
import com.alibaba.druid.sql.dialect.sqlserver.ast.expr.SQLServerObjectReferenceExpr;
import com.alibaba.druid.sql.visitor.SQLASTOutputVisitor;

public class SQLServerOutputVisitor extends SQLASTOutputVisitor implements SQLServerASTVisitor {

    public SQLServerOutputVisitor(Appendable appender){
        super(appender);
    }

    public boolean visit(SQLServerSelectQueryBlock x) {
        print("SELECT ");

        if (SQLSetQuantifier.ALL == x.getDistionOption()) {
            print("ALL ");
        } else if (SQLSetQuantifier.DISTINCT == x.getDistionOption()) {
            print("DISTINCT ");
        } else if (SQLSetQuantifier.UNIQUE == x.getDistionOption()) {
            print("UNIQUE ");
        }

        if (x.getTop() != null) {
            x.getTop().accept(this);
            print(' ');
        }

        printSelectList(x.getSelectList());

        if (x.getInto() != null) {
            println();
            print("INTO ");
            x.getInto().accept(this);
        }

        if (x.getFrom() != null) {
            println();
            print("FROM ");
            x.getFrom().accept(this);
        }

        if (x.getWhere() != null) {
            println();
            print("WHERE ");
            x.getWhere().setParent(x);
            x.getWhere().accept(this);
        }

        if (x.getGroupBy() != null) {
            println();
            x.getGroupBy().accept(this);
        }

        return false;
    }

    @Override
    public void endVisit(SQLServerSelectQueryBlock x) {

    }

    @Override
    public boolean visit(SQLServerTop x) {
        print("TOP ");
        x.getExpr().accept(this);
        if (x.isPercent()) {
            print(" PERCENT");
        }
        return false;
    }

    @Override
    public void endVisit(SQLServerTop x) {

    }

    @Override
    public boolean visit(SQLServerObjectReferenceExpr x) {
        print(x.toString());
        return false;
    }

    @Override
    public void endVisit(SQLServerObjectReferenceExpr x) {
    }

    public boolean visit(SQLExprTableSource x) {
        x.getExpr().accept(this);

        if (x.getHints() != null && x.getHints().size() > 0) {
            print(" WITH (");
            printAndAccept(x.getHints(), ", ");
            print(")");
        }

        if (x.getAlias() != null) {
            print(' ');
            print(x.getAlias());
        }

        return false;
    }

    @Override
    public boolean visit(SQLServerOutput x) {
        print("OUTPUT ");
        printSelectList(x.getSelectList());

        if (x.getInto() != null) {
            incrementIndent();
            println();
            print("INTO ");
            x.getInto().accept(this);

            if (x.getColumns().size() > 0) {
                incrementIndent();
                println();
                print("(");
                for (int i = 0, size = x.getColumns().size(); i < size; ++i) {
                    if (i != 0) {
                        if (i % 5 == 0) {
                            println();
                        }
                        print(", ");
                    }

                    x.getColumns().get(i).accept(this);
                }
                print(")");
                decrementIndent();
            }
        }
        decrementIndent();
        return false;
    }

    @Override
    public void endVisit(SQLServerOutput x) {

    }
    
    @Override
    public void endVisit(SQLServerSelect x) {
        
    }

    @Override
    public boolean visit(SQLServerSelect x) {
        super.visit(x);
        if (x.isForBrowse()) {
            println();
            print("FOR BROWSE");
        }
        
        if (x.getForXmlOptions().size() > 0) {
            println();
            print("FOR XML ");
            for (int i = 0; i < x.getForXmlOptions().size(); ++i) {
                if (i != 0) {
                    print(", ");
                    print(x.getForXmlOptions().get(i));
                }
            }
        }
        
        if (x.getOffset() != null) {
            println();
            print("OFFSET ");
            x.getOffset().accept(this);
            print(" ROWS");
            
            if (x.getRowCount() != null) {
                print(" FETCH NEXT ");
                x.getRowCount().accept(this);
                print(" ROWS ONLY");
            }
        }
        return false;
    }
}