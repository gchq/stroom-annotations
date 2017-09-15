import {
    REQUEST_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION,
    RECEIVE_UPDATE_ANNOTATION_FAILED
} from '../actions/updateAnnotation'

import {
    REQUEST_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION,
    RECEIVE_CREATE_ANNOTATION_FAILED
} from '../actions/createAnnotation'

import {
    REQUEST_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION,
    RECEIVE_FETCH_ANNOTATION_FAILED
} from '../actions/fetchAnnotation'

import {
    REQUEST_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION,
    RECEIVE_REMOVE_ANNOTATION_FAILED
} from '../actions/removeAnnotation'

const defaultState = {
    isFetching: false,
    isClean: true,
    errorMsg: undefined,
    pendingUpdates: 0,
    annotation: {}
}

const annotation = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case RECEIVE_REMOVE_ANNOTATION:
            return defaultState

        case RECEIVE_REMOVE_ANNOTATION_FAILED:
            return {
                    isFetching: false,
                    isClean: false,
                    errorMsg: action.errorMsg,
                    lastUpdated: action.receivedAt
                  }

        case REQUEST_CREATE_ANNOTATION:
        case REQUEST_FETCH_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: true,
                    isClean: false,
                    errorMsg: undefined,
                    annotationId: action.id
                  })

        case REQUEST_UPDATE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    pendingUpdates: state.pendingUpdates + 1,
                    errorMsg: undefined,
                    annotation: {
                        ...state.annotation,
                        ...action.annotation,
                    }
                  })

        case REQUEST_REMOVE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    errorMsg: undefined
                  })

        case RECEIVE_CREATE_ANNOTATION:
        case RECEIVE_FETCH_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: true,
                    errorMsg: undefined,
                    id: action.id,
                    annotation: action.annotation,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_UPDATE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: true,
                    errorMsg: undefined,
                    id: action.id,
                    pendingUpdates: state.pendingUpdates - 1,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_CREATE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    errorMsg: action.errorMsg,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_UPDATE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    pendingUpdates: state.pendingUpdates - 1,
                    errorMsg: action.errorMsg,
                    lastUpdated: action.receivedAt
                  })

        // Of no real concern, it means no annotation exists, so the option to create one will be presented.
        case RECEIVE_FETCH_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: true,
                    lastUpdated: action.receivedAt
                  })
        default:
            return state;
    }

}

export default annotation