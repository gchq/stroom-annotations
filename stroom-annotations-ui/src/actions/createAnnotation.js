import fetch from 'isomorphic-fetch'

export const REQUEST_CREATE_ANNOTATION = 'REQUEST_CREATE_ANNOTATION'

export const requestCreateAnnotation = (apiCallId, id) => ({
    type: REQUEST_CREATE_ANNOTATION,
    id,
    apiCallId
})

export const RECEIVE_CREATE_ANNOTATION = 'RECEIVE_CREATE_ANNOTATION'

export const receiveCreateAnnotation = (apiCallId, id, annotation) => ({
    type: RECEIVE_CREATE_ANNOTATION,
    id,
    annotation,
    apiCallId
})

export const RECEIVE_CREATE_ANNOTATION_FAILED = 'RECEIVE_CREATE_ANNOTATION_FAILED'

export const receiveCreateAnnotationFailed = (apiCallId, message) => ({
    type: RECEIVE_CREATE_ANNOTATION_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const createAnnotation = (indexUuid, id) => {
    return function(dispatch) {
        const thisApiCallId = `createAnnotation-${apiCallId}`
        apiCallId += 1

        dispatch(requestCreateAnnotation(thisApiCallId, id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${indexUuid}/${id}`, {method: "POST"})
            .then(
                response => {
                    if (!response.ok) {
                        throw new Error(response.statusText)
                    }
                    return response.json()
                }
            )
            .then(json => {
                if (json.id) {
                    dispatch(receiveCreateAnnotation(thisApiCallId, id, json))
                }
            }).catch(error => {
                dispatch(receiveCreateAnnotationFailed(thisApiCallId, error.message))
            })
    }
}
