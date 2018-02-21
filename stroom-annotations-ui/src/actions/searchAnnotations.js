import fetch from 'isomorphic-fetch'

import { sendToSnackbar } from './snackBar'

export const SELECT_ROW = 'SELECT_ROW'

export const changeSelectedRow = (annotationId) => ({
    type: SELECT_ROW,
    annotationId
})

export const REQUEST_SEARCH_ANNOTATIONS = 'REQUEST_SEARCH_ANNOTATIONS';

export const requestSearchAnnotations = (apiCallId, searchTerm) => ({
    type: REQUEST_SEARCH_ANNOTATIONS,
    searchTerm,
    apiCallId
})

export const REQUEST_MORE_ANNOTATIONS = 'REQUEST_MORE_ANNOTATIONS';

export const requestMoreAnnotations = (apiCallId) => ({
    type: REQUEST_MORE_ANNOTATIONS,
    apiCallId
})


export const RECEIVE_SEARCH_ANNOTATIONS = 'RECEIVE_SEARCH_ANNOTATIONS';

export const receiveSearchAnnotations = (apiCallId, json, append) => ({
    type: RECEIVE_SEARCH_ANNOTATIONS,
    annotations: json,
    apiCallId,
    append
})

export const RECEIVE_SEARCH_ANNOTATIONS_FAILED = 'RECEIVE_SEARCH_ANNOTATIONS_FAILED';

export const receiveSearchAnnotationsFailed = (apiCallId, message) => ({
    type: RECEIVE_SEARCH_ANNOTATIONS_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const searchAnnotations = (indexUuid, searchTermRaw) => {

    let searchTerm = searchTermRaw ? searchTermRaw : ''

    return function(dispatch, getState) {
        const thisApiCallId = `searchAnnotations-${apiCallId}`
        apiCallId += 1

        dispatch(requestSearchAnnotations(thisApiCallId, searchTerm));

        const state = getState()
        const jwsToken = state.authentication.idToken

        return fetch(`${state.config.annotationsServiceUrl}/search/${indexUuid}/?q=${searchTerm}`, {
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + jwsToken
            },
            mode: 'cors'
        })
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                },
                error => {
                    dispatch(receiveSearchAnnotationsFailed(thisApiCallId, error))
                    dispatch(sendToSnackbar('Failed to Search Annotations ' + error))
                }
            )
            .then(json => dispatch(receiveSearchAnnotations(thisApiCallId, json, false)) )
    }
}

export const moreAnnotations = (indexUuid) => {

    return function(dispatch, getState) {
        const thisApiCallId = `searchAnnotations-${apiCallId}`
        apiCallId += 1

        dispatch(requestMoreAnnotations(thisApiCallId));

        let state = getState()
        const jwsToken = state.authentication.idToken

        return fetch(`${state.config.annotationsServiceUrl}/search/${indexUuid}/?q=${state.manageAnnotations.searchTerm}&seekPosition=${state.manageAnnotations.annotations.length}`, {
            headers: {
                'Accept': 'application/json',
                'Authorization': 'Bearer ' + jwsToken
            },
            mode: 'cors'
        })
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                },
                error => {
                    dispatch(receiveSearchAnnotationsFailed(thisApiCallId, error))
                    dispatch(sendToSnackbar('Failed to Fetch More Annotations ' + error))
                }
            )
            .then(json => dispatch(receiveSearchAnnotations(thisApiCallId, json, true)) )
    }
}
