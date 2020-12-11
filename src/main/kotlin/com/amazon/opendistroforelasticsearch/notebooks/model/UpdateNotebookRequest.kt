/*
 * Copyright 2020 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */

package com.amazon.opendistroforelasticsearch.notebooks.model

import com.amazon.opendistroforelasticsearch.notebooks.NotebooksPlugin.Companion.LOG_PREFIX
import com.amazon.opendistroforelasticsearch.notebooks.model.RestTag.NOTEBOOK_FIELD
import com.amazon.opendistroforelasticsearch.notebooks.model.RestTag.NOTEBOOK_ID_FIELD
import com.amazon.opendistroforelasticsearch.notebooks.util.createJsonParser
import com.amazon.opendistroforelasticsearch.notebooks.util.logger
import org.elasticsearch.action.ActionRequest
import org.elasticsearch.action.ActionRequestValidationException
import org.elasticsearch.common.io.stream.StreamInput
import org.elasticsearch.common.io.stream.StreamOutput
import org.elasticsearch.common.xcontent.ToXContent
import org.elasticsearch.common.xcontent.ToXContentObject
import org.elasticsearch.common.xcontent.XContentBuilder
import org.elasticsearch.common.xcontent.XContentFactory
import org.elasticsearch.common.xcontent.XContentParser
import org.elasticsearch.common.xcontent.XContentParser.Token
import org.elasticsearch.common.xcontent.XContentParserUtils
import java.io.IOException

/**
 * Report Definition-update request.
 * reportDefinitionId is from request query params
 * <pre> JSON format
 * {@code
 * {
 *   "reportDefinitionId":"reportDefinitionId",
 *   "reportDefinition":{
 *      // refer [com.amazon.opendistroforelasticsearch.notebooks.model.ReportDefinition]
 *   }
 * }
 * }</pre>
 */
internal class UpdateNotebookRequest : ActionRequest, ToXContentObject {
    val reportDefinitionId: String
    val reportDefinition: ReportDefinition

    companion object {
        private val log by logger(CreateNotebookRequest::class.java)
    }

    constructor(reportDefinitionId: String, reportDefinition: ReportDefinition) : super() {
        this.reportDefinitionId = reportDefinitionId
        this.reportDefinition = reportDefinition
    }

    @Throws(IOException::class)
    constructor(input: StreamInput) : this(input.createJsonParser())

    /**
     * Parse the data from parser and create [UpdateNotebookRequest] object
     * @param parser data referenced at parser
     */
    constructor(parser: XContentParser, useReportDefinitionId: String? = null) : super() {
        var reportDefinitionId: String? = useReportDefinitionId
        var reportDefinition: ReportDefinition? = null
        XContentParserUtils.ensureExpectedToken(Token.START_OBJECT, parser.currentToken(), parser)
        while (Token.END_OBJECT != parser.nextToken()) {
            val fieldName = parser.currentName()
            parser.nextToken()
            when (fieldName) {
                NOTEBOOK_ID_FIELD -> reportDefinitionId = parser.text()
                NOTEBOOK_FIELD -> reportDefinition = ReportDefinition.parse(parser)
                else -> {
                    parser.skipChildren()
                    log.info("$LOG_PREFIX:Skipping Unknown field $fieldName")
                }
            }
        }
        reportDefinitionId ?: throw IllegalArgumentException("$NOTEBOOK_ID_FIELD field absent")
        reportDefinition ?: throw IllegalArgumentException("$NOTEBOOK_FIELD field absent")
        this.reportDefinitionId = reportDefinitionId
        this.reportDefinition = reportDefinition
    }

    /**
     * {@inheritDoc}
     */
    @Throws(IOException::class)
    override fun writeTo(output: StreamOutput) {
        toXContent(XContentFactory.jsonBuilder(output), ToXContent.EMPTY_PARAMS)
    }

    /**
     * create XContentBuilder from this object using [XContentFactory.jsonBuilder()]
     * @param params XContent parameters
     * @return created XContentBuilder object
     */
    fun toXContent(params: ToXContent.Params = ToXContent.EMPTY_PARAMS): XContentBuilder? {
        return toXContent(XContentFactory.jsonBuilder(), params)
    }

    /**
     * {@inheritDoc}
     */
    override fun toXContent(builder: XContentBuilder?, params: ToXContent.Params?): XContentBuilder {
        return builder!!.startObject()
            .field(NOTEBOOK_ID_FIELD, reportDefinitionId)
            .field(NOTEBOOK_FIELD, reportDefinition)
            .endObject()
    }

    /**
     * {@inheritDoc}
     */
    override fun validate(): ActionRequestValidationException? {
        return null
    }
}
