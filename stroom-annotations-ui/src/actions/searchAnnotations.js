import fetch from 'isomorphic-fetch'

export const REQUEST_SEARCH_ANNOTATIONS = 'REQUEST_SEARCH_ANNOTATIONS';

export const requestSearchAnnotations = (searchTerm) => ({
    type: REQUEST_SEARCH_ANNOTATIONS,
    searchTerm
})

export const RECEIVE_SEARCH_ANNOTATIONS = 'RECEIVE_SEARCH_ANNOTATIONS';

export const receiveSearchAnnotations = (json) => ({
    type: RECEIVE_SEARCH_ANNOTATIONS,
    annotations: json,
    receivedAt: Date.now()
})

export const RECEIVE_SEARCH_ANNOTATIONS_FAILED = 'RECEIVE_SEARCH_ANNOTATIONS_FAILED';

export const receiveSearchAnnotationsFailed = (message) => ({
    type: RECEIVE_SEARCH_ANNOTATIONS_FAILED,
    message,
    receivedAt: Date.now()
})

export const searchAnnotations = (searchTermRaw) => {

    let searchTerm = searchTermRaw ? searchTermRaw : ''

    return function(dispatch) {
        dispatch(requestSearchAnnotations(searchTerm));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/search?q=${searchTerm}`)
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                }
            )
            .then(json => dispatch(receiveSearchAnnotations(json)) )
            .catch(error => {
                dispatch(receiveSearchAnnotationsFailed(error))
            })
    }
}
