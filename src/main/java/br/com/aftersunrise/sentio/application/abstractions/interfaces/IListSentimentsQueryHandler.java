package br.com.aftersunrise.sentio.application.abstractions.interfaces;

import br.com.aftersunrise.sentio.application.abstractions.models.PagedResult;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentResponse;
import br.com.aftersunrise.sentio.application.analysis.queries.ListSentimentsQuery;

public interface IListSentimentsQueryHandler extends IHandler<ListSentimentsQuery, PagedResult<SentimentResponse>> { }

