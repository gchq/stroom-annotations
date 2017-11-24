import fetch from 'isomorphic-fetch'

export const EDIT_ANNOTATION = 'EDIT_ANNOTATION'

export const editAnnotation = (id, updates) => ({
    type: EDIT_ANNOTATION,
    id,
    updates
})

export const REQUEST_UPDATE_ANNOTATION = 'REQUEST_UPDATE_ANNOTATION'

export const requestUpdateAnnotation = (apiCallId, id, annotation) => ({
    type: REQUEST_UPDATE_ANNOTATION,
    id,
    annotation,
    apiCallId
})

export const RECEIVE_UPDATE_ANNOTATION = 'RECEIVE_UPDATE_ANNOTATION';
 
export const receiveUpdateAnnotation = (apiCallId, id, annotation) => ({
    type: RECEIVE_UPDATE_ANNOTATION,
    id,
    annotation,
    apiCallId
})

export const RECEIVE_UPDATE_ANNOTATION_FAILED = 'RECEIVE_UPDATE_ANNOTATION_FAILED';

export const receiveUpdateAnnotationFailed = (apiCallId, message) => ({
    type: RECEIVE_UPDATE_ANNOTATION_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const updateAnnotation = (indexUuid, id, annotation) => {
    return function(dispatch) {
        const thisApiCallId = `updateAnnotation-${apiCallId}`
        apiCallId += 1

        dispatch(requestUpdateAnnotation(thisApiCallId, id, annotation));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${indexUuid}/${id}`,
            {
                method: "PUT",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(annotation)
            }
        )
              .then(
                response => response.json(),
                // Do not use catch, because that will also catch
                // any errors in the dispatch and resulting render,
                // causing an loop of 'Unexpected batch number' errors.
                // https://github.com/facebook/react/issues/6895
                error => console.log('An error occured.', error)
              )
              .then(json => {
                if (json.id) {
                    dispatch(receiveUpdateAnnotation(thisApiCallId, id, json))
                } else {
                    dispatch(receiveUpdateAnnotationFailed(thisApiCallId, json.msg))
                }
              })
    }
}
