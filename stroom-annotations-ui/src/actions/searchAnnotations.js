import fetch from 'isomorphic-fetch'

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

    return function(dispatch) {
        const thisApiCallId = `searchAnnotations-${apiCallId}`
        apiCallId += 1

        dispatch(requestSearchAnnotations(thisApiCallId, searchTerm));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/search/${indexUuid}/?q=${searchTerm}`)
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                }
            )
            .then(json => dispatch(receiveSearchAnnotations(thisApiCallId, json, false)) )
            .catch(error => {
                dispatch(receiveSearchAnnotationsFailed(thisApiCallId, error))
            })
    }
}

export const moreAnnotations = (indexUuid) => {

    return function(dispatch, getState) {
        const thisApiCallId = `searchAnnotations-${apiCallId}`
        apiCallId += 1

        dispatch(requestMoreAnnotations(thisApiCallId));

        let state = getState().manageAnnotations

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/search/${indexUuid}/?q=${state.searchTerm}&seekPosition=${state.annotations.length}`)
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                }
            )
            .then(json => dispatch(receiveSearchAnnotations(thisApiCallId, json, true)) )
            .catch(error => {
                dispatch(receiveSearchAnnotationsFailed(thisApiCallId, error))
            })
    }
}
