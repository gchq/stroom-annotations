import fetch from 'isomorphic-fetch'

import { fetchAnnotationHistory } from './fetchAnnotationHistory'

export const REQUEST_REMOVE_ANNOTATION = 'REQUEST_REMOVE_ANNOTATION'

export const requestRemoveAnnotation = (apiCallId, id) => ({
    type: REQUEST_REMOVE_ANNOTATION,
    id,
    apiCallId
})

export const RECEIVE_REMOVE_ANNOTATION = 'RECEIVE_REMOVE_ANNOTATION'

export const receiveRemoveAnnotation = (apiCallId, id) => ({
    type: RECEIVE_REMOVE_ANNOTATION,
    id,
    apiCallId
})

export const RECEIVE_REMOVE_ANNOTATION_FAILED = 'RECEIVE_REMOVE_ANNOTATION_FAILED'

export const receiveRemoveAnnotationFailed = (apiCallId, message) => ({
    type: RECEIVE_REMOVE_ANNOTATION_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const removeAnnotation = (indexUuid, id) => {
    return function(dispatch) {
        const thisApiCallId = `removeAnnotation-${apiCallId}`
        apiCallId += 1

        dispatch(requestRemoveAnnotation(thisApiCallId, id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${indexUuid}/${id}`,
            {
                method: "DELETE"
            }
        )
              .then(
                response => {
                    dispatch(receiveRemoveAnnotation(thisApiCallId, id))
                    dispatch(fetchAnnotationHistory(indexUuid, id))
                },
                error => dispatch(receiveRemoveAnnotationFailed(thisApiCallId, error))
              )
    }
}
