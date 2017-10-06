import fetch from 'isomorphic-fetch'

export const REQUEST_FETCH_STATUS_VALUES = 'REQUEST_FETCH_STATUS_VALUES';

export const requestFetchStatusValues = (apiCallId, id) => ({
    type: REQUEST_FETCH_STATUS_VALUES,
    id,
    apiCallId
})

export const RECEIVE_FETCH_STATUS_VALUES = 'RECEIVE_FETCH_STATUS_VALUES';

export const receiveFetchStatusValues = (apiCallId, values) => ({
    type: RECEIVE_FETCH_STATUS_VALUES,
    values,
    apiCallId
})

export const RECEIVE_FETCH_STATUS_VALUES_FAILED = 'RECEIVE_FETCH_STATUS_VALUES_FAILED';

export const receiveFetchStatusValuesFailed = (apiCallId, message) => ({
    type: RECEIVE_FETCH_STATUS_VALUES_FAILED,
    message,
    apiCallId
})

let apiCallId = 0

export const fetchStatusValues = (id) => {
    return function(dispatch) {
        const thisApiCallId = `fetchStatusValues-${apiCallId}`
        apiCallId += 1

        dispatch(requestFetchStatusValues(thisApiCallId, id));

        return fetch(`${process.env.REACT_APP_ANNOTATIONS_URL}/static/statusValues`)
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
                    dispatch(receiveFetchStatusValues(thisApiCallId, json))
                } else {
                    dispatch(receiveFetchStatusValuesFailed(thisApiCallId, json.msg))
                }
              })
    }
}
