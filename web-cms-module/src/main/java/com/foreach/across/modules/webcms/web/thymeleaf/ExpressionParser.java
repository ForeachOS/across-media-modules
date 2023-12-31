/*
 * Copyright 2017 the original author or authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.foreach.across.modules.webcms.web.thymeleaf;

import org.apache.commons.lang3.StringUtils;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.standard.expression.*;
import org.thymeleaf.util.FastStringWriter;

import static org.thymeleaf.standard.expression.FragmentExpression.*;

/**
 * Helper for parsing attribute values to Thymeleaf expressions
 *
 * @author Arne Vandamme
 * @since 0.0.7
 */
@FunctionalInterface
interface ExpressionParser
{
	/**
	 * If the value corresponds with an expression, parse and execute it.
	 * Else simply return the original value. Will attempts to render-aside
	 * any fragment expression.
	 *
	 * @param original value - possibly an expression
	 * @return result
	 */
	Object parse( String original );

	static ExpressionParser create( ITemplateContext context ) {
		IStandardExpressionParser parser = StandardExpressions.getExpressionParser( context.getConfiguration() );

		return original -> {
			if ( StringUtils.length( original ) >= 3 && ( original.charAt( 1 ) == '{' && original.charAt( original.length() - 1 ) == '}' )
					|| ( original.charAt( 0 ) == '|' && original.charAt( original.length() - 1 ) == '|' ) ) {

				IStandardExpression expression = parser.parseExpression( context, original );

				if ( expression instanceof FragmentExpression ) {
					ExecutedFragmentExpression executedFragmentExpression = createExecutedFragmentExpression( context, (FragmentExpression) expression );
					Fragment fragment = resolveExecutedFragmentExpression( context, executedFragmentExpression, true );

					FastStringWriter sw = new FastStringWriter( 200 );
					context.getConfiguration().getTemplateManager().process( fragment.getTemplateModel(), context, sw );
					return sw.toString();
				}

				return expression.execute( context );
			}

			return original;
		};
	}
}
