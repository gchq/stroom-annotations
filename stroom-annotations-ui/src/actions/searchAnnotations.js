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

export const receiveSearchAnnotationsFailed = (errorMsg) => ({
    type: RECEIVE_SEARCH_ANNOTATIONS_FAILED,
    errorMsg,
    receivedAt: Date.now()
})

export const searchAnnotations = (searchTermRaw) => {

    let searchTerm = searchTermRaw ? searchTermRaw : ''

    return function(dispatch) {
        dispatch(requestSearchAnnotations(searchTerm));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/search?q=${searchTerm}`)
            .then(
                response => response.json(),
                // Do not use catch, because that will also catch
                // any errors in the dispatch and resulting render,
                // causing an loop of 'Unexpected batch number' errors.
                // https://github.com/facebook/react/issues/6895
                error => console.log('An error occured.', error)
            )
            .then(json => {
                if (json) {
                    dispatch(receiveSearchAnnotations(json))
                } else {
                    dispatch(receiveSearchAnnotationsFailed(json.msg))
                }
            })
    }
}
