import fetch from 'isomorphic-fetch'

export const REQUEST_FETCH_STATUS_VALUES = 'REQUEST_FETCH_STATUS_VALUES';

export const requestFetchStatusValues = (id) => ({
    type: REQUEST_FETCH_STATUS_VALUES,
    id
})

export const RECEIVE_FETCH_STATUS_VALUES = 'RECEIVE_FETCH_STATUS_VALUES';

export const receiveFetchStatusValues = (values) => ({
    type: RECEIVE_FETCH_STATUS_VALUES,
    values,
    receivedAt: Date.now()
})

export const RECEIVE_FETCH_STATUS_VALUES_FAILED = 'RECEIVE_FETCH_STATUS_VALUES_FAILED';

export const receiveFetchStatusValuesFailed = (errorMsg) => ({
    type: RECEIVE_FETCH_STATUS_VALUES_FAILED,
    errorMsg,
    receivedAt: Date.now()
})

export const fetchStatusValues = (id) => {
    return function(dispatch) {
        dispatch(requestFetchStatusValues(id));

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
                    dispatch(receiveFetchStatusValues(json))
                } else {
                    dispatch(receiveFetchStatusValuesFailed(json.msg))
                }
              })
    }
}
