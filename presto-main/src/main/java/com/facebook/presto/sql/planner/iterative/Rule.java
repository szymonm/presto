/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.presto.sql.planner.iterative;

import com.facebook.presto.Session;
import com.facebook.presto.matching.Matchable;
import com.facebook.presto.matching.Pattern;
import com.facebook.presto.sql.planner.PlanNodeIdAllocator;
import com.facebook.presto.sql.planner.SymbolAllocator;
import com.facebook.presto.sql.planner.plan.PlanNode;

import java.util.Optional;

public interface Rule
        extends Matchable
{
    /**
     * Returns a pattern to which plan nodes this rule applies.
     */
    default Pattern getPattern()
    {
        return Pattern.any();
    }

    Optional<PlanNode> apply(PlanNode node, Context context);

    interface Context
    {
        Lookup getLookup();

        PlanNodeIdAllocator getIdAllocator();

        SymbolAllocator getSymbolAllocator();

        Session getSession();
    }
}
