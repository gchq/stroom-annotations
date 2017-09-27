import fetch from 'isomorphic-fetch'

export const EDIT_ANNOTATION = 'EDIT_ANNOTATION'

export const editAnnotation = (id, updates) => ({
    type: EDIT_ANNOTATION,
    id,
    updates
})

export const REQUEST_UPDATE_ANNOTATION = 'REQUEST_UPDATE_ANNOTATION'

export const requestUpdateAnnotation = (id, annotation) => ({
    type: REQUEST_UPDATE_ANNOTATION,
    id,
    annotation
})

export const RECEIVE_UPDATE_ANNOTATION = 'RECEIVE_UPDATE_ANNOTATION';
 
 export const receiveUpdateAnnotation = (id, annotation) => ({
     type: RECEIVE_UPDATE_ANNOTATION,
     id,
     annotation,
     receivedAt: Date.now()
 })

 export const RECEIVE_UPDATE_ANNOTATION_FAILED = 'RECEIVE_UPDATE_ANNOTATION_FAILED';

 export const receiveUpdateAnnotationFailed = (errorMsg) => ({
     type: RECEIVE_UPDATE_ANNOTATION_FAILED,
     errorMsg,
     receivedAt: Date.now()
 })

export const updateAnnotation = (id, annotation) => {
    return function(dispatch) {
        dispatch(requestUpdateAnnotation(id, annotation));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/single/${id}`,
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
                    dispatch(receiveUpdateAnnotation(id, json))
                } else {
                    dispatch(receiveUpdateAnnotationFailed(json.msg))
                }
              })
    }
}
