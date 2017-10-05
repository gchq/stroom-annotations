import {
    EDIT_ANNOTATION,
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
    RECEIVE_FETCH_ANNOTATION_NOT_EXIST,
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
    pendingUpdates: 0,
    annotation: {}
}

const singleAnnotation = (
    state = defaultState,
    action
 ) => {
    switch(action.type) {
        case RECEIVE_REMOVE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: true,
                    lastUpdated: action.receivedAt,
                    annotation: {}
                })

        case RECEIVE_REMOVE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    lastUpdated: action.receivedAt,
                    annotation: {}
                  })

        case REQUEST_CREATE_ANNOTATION:
        case REQUEST_FETCH_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: true,
                    isClean: true,
                    annotationId: action.id
                  })

        case EDIT_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    annotation: {
                        ...state.annotation,
                        ...action.updates,
                    }
                  })

        case REQUEST_UPDATE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    pendingUpdates: state.pendingUpdates + 1,
                    message: undefined,
                    annotation: {
                        ...state.annotation,
                        ...action.annotation,
                    }
                  })

        case REQUEST_REMOVE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    message: undefined
                  })

        case RECEIVE_CREATE_ANNOTATION:
        case RECEIVE_FETCH_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: true,
                    id: action.id,
                    annotation: action.annotation,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_FETCH_ANNOTATION_NOT_EXIST:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: true,
                    id: action.id,
                    annotation: {},
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_UPDATE_ANNOTATION:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: true,
                    id: action.id,
                    pendingUpdates: state.pendingUpdates - 1,
                    annotation: action.annotation,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_CREATE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    lastUpdated: action.receivedAt
                  })

        case RECEIVE_UPDATE_ANNOTATION_FAILED:
            return Object.assign({}, state, {
                    isFetching: false,
                    isClean: false,
                    pendingUpdates: state.pendingUpdates - 1,
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

export default singleAnnotation