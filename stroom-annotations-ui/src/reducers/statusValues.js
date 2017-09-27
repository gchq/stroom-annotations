import {
    REQUEST_FETCH_STATUS_VALUES,
    RECEIVE_FETCH_STATUS_VALUES,
    RECEIVE_FETCH_STATUS_VALUES_FAILED
} from '../actions/fetchStatusValues'

const defaultState = []

const statusValues = (
    state = defaultState,
    action
) => {
    switch(action.type) {
        case REQUEST_FETCH_STATUS_VALUES:
            return state
        case RECEIVE_FETCH_STATUS_VALUES:
            return action.values
        case RECEIVE_FETCH_STATUS_VALUES_FAILED:
            return state
        default:
            return state
    }
}

export default statusValues